import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Сервис бронирования. Проверка доступности (isAvailable / findConflict) отделена от создания объекта (book)
 * и не изменяет репозиторий.
 */
public class BookingService {

    public static final Duration MIN_DURATION = Duration.ofMinutes(15);
    /** Индивидуальный вариант 1: максимальная длительность = 10 часов (базовое правило — 8 часов). */
    public static final Duration MAX_DURATION = Duration.ofHours(10);

    private final BookingRepository repository;
    /** Буфер между бронированиями одной комнаты; по умолчанию нулевой (стык «конец = начало» допустим). */
    private final Duration buffer;
    private int sequence;

    public BookingService(BookingRepository repository) {
        this(repository, Duration.ZERO);
    }

    public BookingService(BookingRepository repository, Duration buffer) {
        if (buffer == null || buffer.isNegative()) {
            throw new IllegalArgumentException("буфер не может быть отрицательным");
        }
        this.repository = repository;
        this.buffer = buffer;
    }

    public Booking book(String roomId, LocalDateTime start, LocalDateTime end, LocalDateTime clockNow) {
        validateInterval(start, end, clockNow);
        Optional<Booking> conflict = findConflict(roomId, start, end);
        if (conflict.isPresent()) {
            throw new BookingConflictException(conflict.get());
        }
        Booking booking = new Booking("B-" + (++sequence), roomId, start, end);
        repository.save(booking);
        return booking;
    }

    public boolean isAvailable(String roomId, LocalDateTime start, LocalDateTime end) {
        return findConflict(roomId, start, end).isEmpty();
    }

    public Optional<Booking> findConflict(String roomId, LocalDateTime start, LocalDateTime end) {
        for (Booking existing : repository.findByRoom(roomId)) {
            if (overlaps(existing, start, end)) {
                return Optional.of(existing);
            }
        }
        return Optional.empty();
    }

    /** startA < endB && startB < endA; буфер расширяет существующее бронирование в обе стороны. */
    private boolean overlaps(Booking existing, LocalDateTime start, LocalDateTime end) {
        return start.isBefore(existing.getEnd().plus(buffer)) && existing.getStart().minus(buffer).isBefore(end);
    }

    static void validateInterval(LocalDateTime start, LocalDateTime end, LocalDateTime clockNow) {
        if (start == null || end == null || clockNow == null) {
            throw new IllegalArgumentException("начало, окончание и текущее время обязательны");
        }
        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("окончание " + end + " должно быть строго позже начала " + start);
        }
        if (start.isBefore(clockNow)) {
            throw new IllegalArgumentException("нельзя бронировать время в прошлом: начало " + start + ", сейчас " + clockNow);
        }
        Duration duration = Duration.between(start, end);
        if (duration.compareTo(MIN_DURATION) < 0 || duration.compareTo(MAX_DURATION) > 0) {
            throw new IllegalArgumentException("длительность " + duration.toMinutes() + " мин вне диапазона "
                    + MIN_DURATION.toMinutes() + ".." + MAX_DURATION.toMinutes() + " мин");
        }
    }
}
