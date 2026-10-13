import java.nio.file.Path;

/** Точка входа: java ImportApp [путь к файлу], по умолчанию data/sample.csv */
public class ImportApp {

    public static void main(String[] args) {
        Path file = Path.of(args.length > 0 ? args[0] : "data/sample.csv");
        ClientRepository repository = new ClientRepository();
        ClientImporter importer = new ClientImporter(new ClientCsvParser(), new ClientValidator(), repository);
        try {
            ImportResult result = importer.importFile(file);
            System.out.println(result);
            System.out.println("В хранилище клиентов: " + repository.count());
        } catch (ImportException e) {
            System.out.println("Импорт прерван: " + e.getMessage());
            System.exit(2);
        }
    }
}
