import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/** Хранилище клиентов в памяти; ключ — ИНН. */
public class ClientRepository {

    private final Map<String, Client> byInn = new LinkedHashMap<>();

    public void save(Client client) {
        byInn.put(client.inn(), client);
    }

    public Optional<Client> findByInn(String inn) {
        return Optional.ofNullable(byInn.get(inn));
    }

    public int count() {
        return byInn.size();
    }
}
