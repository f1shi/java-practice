import java.util.ArrayList;
import java.util.List;

/** Хранилище бронирований в памяти. */
public class BookingRepository {

    private final List<Booking> bookings = new ArrayList<>();

    public void save(Booking booking) {
        bookings.add(booking);
    }

    /** Новый список бронирований комнаты; изменение результата не влияет на хранилище. */
    public List<Booking> findByRoom(String roomId) {
        List<Booking> result = new ArrayList<>();
        for (Booking booking : bookings) {
            if (booking.getRoomId().equals(roomId)) {
                result.add(booking);
            }
        }
        return result;
    }

    public int count() {
        return bookings.size();
    }
}
