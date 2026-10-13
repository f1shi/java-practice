import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Сервис: хранение заявок и координация сценариев. Правила переходов остаются в BusinessTrip. */
public class BusinessTripService {

    private final Map<String, BusinessTrip> trips = new LinkedHashMap<>();

    public BusinessTrip create(String id, Employee employee, LocalDate start, LocalDate end,
                               String destination, String purpose) {
        if (id != null && trips.containsKey(id.trim())) {
            throw new IllegalArgumentException("командировка " + id + " уже существует");
        }
        BusinessTrip trip = new BusinessTrip(id, employee, start, end);
        trip.updateDetails(destination, purpose, start, end);
        trips.put(trip.getId(), trip);
        return trip;
    }

    public BusinessTrip get(String id) {
        BusinessTrip trip = trips.get(id);
        if (trip == null) {
            throw new IllegalArgumentException("командировка " + id + " не найдена");
        }
        return trip;
    }

    public List<BusinessTrip> findByStatus(TripStatus status) {
        List<BusinessTrip> result = new ArrayList<>();
        for (BusinessTrip trip : trips.values()) {
            if (trip.getStatus() == status) {
                result.add(trip);
            }
        }
        return result;
    }
}
