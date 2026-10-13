import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/** Потокобезопасные агрегаты пакетной обработки. Обновлять можно из любого потока. */
public class BatchResult {

    /** Индивидуальный вариант 3 — дополнительный агрегат: число «объёмных» документов, в которых не менее 20 слов. */
    public static final int LARGE_DOCUMENT_WORDS = 20;

    private final AtomicInteger successCount = new AtomicInteger();
    private final AtomicInteger largeDocuments = new AtomicInteger();
    private final AtomicLong totalWords = new AtomicLong();
    private final ConcurrentMap<String, String> errors = new ConcurrentHashMap<>();

    public void registerSuccess(ProcessingResult result) {
        successCount.incrementAndGet();
        totalWords.addAndGet(result.wordCount());
        if (result.wordCount() >= LARGE_DOCUMENT_WORDS) {
            largeDocuments.incrementAndGet();
        }
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

    /** Агрегат варианта 3: успешно обработанные документы с числом слов >= 20. */
    public int getLargeDocumentCount() {
        return largeDocuments.get();
    }

    /** Копия ошибок, отсортированная по id документа — детерминированный вывод. */
    public Map<String, String> getErrors() {
        return new TreeMap<>(errors);
    }

    @Override
    public String toString() {
        return "успешно: " + getSuccessCount() + ", с ошибкой: " + getErrorCount() + ", слов: " + getTotalWords()
                + ", документов от " + LARGE_DOCUMENT_WORDS + " слов: " + getLargeDocumentCount();
    }
}
