import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Заявка на командировку. Статус меняется только доменными методами submit/approve/reject/cancel/complete;
 * открытого setStatus нет. Недопустимый переход -> IllegalStateException, неверные данные -> IllegalArgumentException.
 * При любой ошибке объект остаётся в прежнем состоянии (проверки выполняются до изменения полей).
 */
public class BusinessTrip {

    /** Минимальная длина причины отклонения (без учёта пробелов по краям). */
    public static final int MIN_REJECTION_REASON_LENGTH = 10;
    /**
     * Индивидуальный вариант 3 — дополнительная проверка даты (модификатор 20):
     * командировка длится не более 20 календарных дней, считая день начала и день окончания.
     */
    public static final int MAX_TRIP_DAYS = 20;

    private final String id;
    private final Employee employee;
    private LocalDate start;
    private LocalDate end;
    private String destination;
    private String purpose;
    private TripStatus status = TripStatus.DRAFT;
    private String rejectionReason;

    public BusinessTrip(String id, Employee employee, LocalDate start, LocalDate end) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id командировки не может быть пустым");
        }
        if (employee == null) {
            throw new IllegalArgumentException("сотрудник не указан");
        }
        checkDates(start, end);
        this.id = id.trim();
        this.employee = employee;
        this.start = start;
        this.end = end;
    }

    /** Редактирование данных возможно только в черновике. */
    public void updateDetails(String destination, String purpose, LocalDate start, LocalDate end) {
        requireStatus("изменить данные", TripStatus.DRAFT);
        checkDates(start, end);
        this.destination = destination == null ? null : destination.trim();
        this.purpose = purpose == null ? null : purpose.trim();
        this.start = start;
        this.end = end;
    }

    public void submit() {
        requireStatus("отправить на согласование", TripStatus.DRAFT);
        if (isBlank(destination)) {
            throw new IllegalArgumentException("не заполнено место назначения");
        }
        if (isBlank(purpose)) {
            throw new IllegalArgumentException("не заполнена цель командировки");
        }
        status = TripStatus.SUBMITTED;
    }

    public void approve() {
        requireStatus("согласовать", TripStatus.SUBMITTED);
        status = TripStatus.APPROVED;
    }

    public void reject(String reason) {
        requireStatus("отклонить", TripStatus.SUBMITTED);
        String trimmed = reason == null ? "" : reason.trim();
        if (trimmed.length() < MIN_REJECTION_REASON_LENGTH) {
            throw new IllegalArgumentException("причина отклонения должна содержать не менее "
                    + MIN_REJECTION_REASON_LENGTH + " символов, получено " + trimmed.length());
        }
        rejectionReason = trimmed;
        status = TripStatus.REJECTED;
    }

    public void cancel() {
        requireStatus("отменить", TripStatus.DRAFT, TripStatus.SUBMITTED, TripStatus.APPROVED);
        status = TripStatus.CANCELLED;
    }

    /** Завершение возможно только после даты окончания: today строго позже end. */
    public void complete(LocalDate today) {
        requireStatus("завершить", TripStatus.APPROVED);
        if (today == null) {
            throw new IllegalArgumentException("текущая дата не указана");
        }
        if (!today.isAfter(end)) {
            throw new IllegalStateException("командировка заканчивается " + end
                    + ", завершить её можно не раньше " + end.plusDays(1));
        }
        status = TripStatus.COMPLETED;
    }

    private void requireStatus(String action, TripStatus... allowed) {
        for (TripStatus s : allowed) {
            if (status == s) {
                return;
            }
        }
        throw new IllegalStateException("нельзя " + action + " командировку " + id + " в статусе " + status);
    }

    private static void checkDates(LocalDate start, LocalDate end) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("даты начала и окончания обязательны");
        }
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("дата окончания " + end + " раньше даты начала " + start);
        }
        long days = ChronoUnit.DAYS.between(start, end) + 1;
        if (days > MAX_TRIP_DAYS) {
            throw new IllegalArgumentException("командировка " + start + ".." + end + " длится " + days
                    + " дн., максимум " + MAX_TRIP_DAYS);
        }
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    public String getId() {
        return id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public LocalDate getStart() {
        return start;
    }

    public LocalDate getEnd() {
        return end;
    }

    public String getDestination() {
        return destination;
    }

    public String getPurpose() {
        return purpose;
    }

    public TripStatus getStatus() {
        return status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    @Override
    public String toString() {
        return id + " " + employee.fullName() + " " + destination + " " + start + ".." + end + " [" + status + "]";
    }
}
