import java.time.Duration;
import java.time.LocalDateTime;

/** Бронирование переговорной комнаты на полуинтервал [start, end). */
public final class Booking {

    private final String id;
    private final String roomId;
    private final LocalDateTime start;
    private final LocalDateTime end;

    public Booking(String id, String roomId, LocalDateTime start, LocalDateTime end) {
        if (id == null || id.isBlank() || roomId == null || roomId.isBlank()) {
            throw new IllegalArgumentException("id бронирования и комнаты обязательны");
        }
        if (start == null || end == null || !end.isAfter(start)) {
            throw new IllegalArgumentException("окончание должно быть строго позже начала: " + start + " .. " + end);
        }
        this.id = id;
        this.roomId = roomId;
        this.start = start;
        this.end = end;
    }

    public Duration duration() {
        return Duration.between(start, end);
    }

    public String getId() {
        return id;
    }

    public String getRoomId() {
        return roomId;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    @Override
    public String toString() {
        return id + " " + roomId + " " + start + " .. " + end;
    }
}
