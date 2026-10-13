import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/** Потокобезопасные агрегаты пакетной обработки. Обновлять можно из любого потока. */
public class BatchResult {

    private final AtomicInteger successCount = new AtomicInteger();
    private final AtomicLong totalWords = new AtomicLong();
    private final ConcurrentMap<String, String> errors = new ConcurrentHashMap<>();

    public void registerSuccess(ProcessingResult result) {
        successCount.incrementAndGet();
        totalWords.addAndGet(result.wordCount());
    }

    public void registerError(String documentId, String reason) {
        errors.merge(documentId, reason, (first, second) -> first + "; " + second);
    }

    public int getSuccessCount() {
        return successCount.get();
    }

    public int getErrorCount() {
        return errors.size();
    }

    /** Дополнительный агрегат: общее число слов в успешно обработанных документах. */
    public long getTotalWords() {
        return totalWords.get();
    }

    /** Копия ошибок, отсортированная по id документа — детерминированный вывод. */
    public Map<String, String> getErrors() {
        return new TreeMap<>(errors);
    }

    @Override
    public String toString() {
        return "успешно: " + getSuccessCount() + ", с ошибкой: " + getErrorCount() + ", слов: " + getTotalWords();
    }
}
