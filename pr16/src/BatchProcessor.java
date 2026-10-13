import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Пакетная обработка на ограниченном пуле потоков: один документ — одна задача Callable.
 * Ошибка отдельной задачи фиксируется в BatchResult и не отменяет остальные.
 */
public class BatchProcessor {

    public static final int MIN_POOL_SIZE = 1;
    public static final int MAX_POOL_SIZE = 8;

    private final DocumentProcessor processor;
    private final int poolSize;

    public BatchProcessor(DocumentProcessor processor, int poolSize) {
        if (processor == null) {
            throw new IllegalArgumentException("обработчик документов обязателен");
        }
        if (poolSize < MIN_POOL_SIZE || poolSize > MAX_POOL_SIZE) {
            throw new IllegalArgumentException("размер пула должен быть от " + MIN_POOL_SIZE + " до "
                    + MAX_POOL_SIZE + ", получено " + poolSize);
        }
        this.processor = processor;
        this.poolSize = poolSize;
    }

    public BatchResult processAll(List<Document> documents) throws InterruptedException {
        BatchResult result = new BatchResult();
        List<Document> unique = skipDuplicates(documents, result);
        ExecutorService executor = Executors.newFixedThreadPool(poolSize);
        try {
            List<Future<ProcessingResult>> futures = new ArrayList<>();
            for (Document document : unique) {
                Callable<ProcessingResult> task = () -> processor.process(document);
                futures.add(executor.submit(task));
            }
            for (int i = 0; i < futures.size(); i++) {
                collect(unique.get(i), futures.get(i), result);
            }
        } finally {
            executor.shutdown();
            if (!executor.awaitTermination(30, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        }
        return result;
    }

    /** Последовательная версия для сравнения времени; правила те же. */
    public BatchResult processSequentially(List<Document> documents) throws InterruptedException {
        BatchResult result = new BatchResult();
        for (Document document : skipDuplicates(documents, result)) {
            try {
                result.registerSuccess(processor.process(document));
            } catch (DocumentProcessingException | RuntimeException e) {
                result.registerError(document.id(), e.getMessage());
            }
        }
        return result;
    }

    /** Ожидание результата одной задачи; ошибка этой задачи локализуется здесь. */
    private static void collect(Document document, Future<ProcessingResult> future, BatchResult result)
            throws InterruptedException {
        try {
            result.registerSuccess(future.get());
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            result.registerError(document.id(), cause.getClass().getSimpleName() + ": " + cause.getMessage());
        }
    }

    /** Повтор id: второй и последующие документы не обрабатываются и фиксируются как ошибка. */
    private static List<Document> skipDuplicates(List<Document> documents, BatchResult result) {
        if (documents == null) {
            throw new IllegalArgumentException("список документов не передан");
        }
        Set<String> seen = new HashSet<>();
        List<Document> unique = new ArrayList<>();
        for (Document document : documents) {
            if (document == null) {
                throw new IllegalArgumentException("список документов содержит null");
            }
            if (seen.add(document.id())) {
                unique.add(document);
            } else {
                result.registerError(document.id(), "дубликат id, повторный документ пропущен");
            }
        }
        return unique;
    }
}
