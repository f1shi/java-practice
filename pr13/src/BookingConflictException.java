/** Запрошенный интервал пересекается с существующим бронированием. */
public class BookingConflictException extends RuntimeException {

    private final String conflictingBookingId;

    public BookingConflictException(Booking conflicting) {
        super("время занято бронированием " + conflicting);
        this.conflictingBookingId = conflicting.getId();
    }

    public String getConflictingBookingId() {
        return conflictingBookingId;
    }
}
