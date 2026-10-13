import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Обрабатывает data/sample.csv (id;content) последовательно и параллельно и сравнивает время.
 * Аргументы: [poolSize] [delayMs]
 */
public class BatchApp {

    public static void main(String[] args) throws IOException, InterruptedException {
        int poolSize = args.length > 0 ? Integer.parseInt(args[0]) : 4;
        long delayMs = args.length > 1 ? Long.parseLong(args[1]) : 100;
        List<Document> documents = load(Path.of("data/sample.csv"));
        BatchProcessor batch = new BatchProcessor(new DocumentProcessor(delayMs), poolSize);

        long t0 = System.nanoTime();
        BatchResult sequential = batch.processSequentially(documents);
        long t1 = System.nanoTime();
        BatchResult parallel = batch.processAll(documents);
        long t2 = System.nanoTime();

        System.out.println("Документов: " + documents.size() + ", пул: " + poolSize + ", задержка: " + delayMs + " мс");
        System.out.printf("Последовательно: %s, время %d мс%n", sequential, (t1 - t0) / 1_000_000);
        System.out.printf("Параллельно:     %s, время %d мс%n", parallel, (t2 - t1) / 1_000_000);
        for (Map.Entry<String, String> e : parallel.getErrors().entrySet()) {
            System.out.println("  ошибка " + e.getKey() + ": " + e.getValue());
        }
    }

    static List<Document> load(Path file) throws IOException {
        List<Document> result = new ArrayList<>();
        List<String> lines = Files.readAllLines(file);
        for (int i = 1; i < lines.size(); i++) {
            String[] p = lines.get(i).split(";", 2);
            result.add(new Document(p[0].trim(), p.length > 1 ? p[1] : ""));
        }
        return result;
    }
}
