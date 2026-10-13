/**
 * Обработка одного документа: подсчёт слов. Длительная операция имитируется задержкой.
 * Не хранит состояния между вызовами, поэтому безопасна для одновременного использования из нескольких потоков.
 */
public class DocumentProcessor {

    private final long simulatedDelayMs;

    public DocumentProcessor(long simulatedDelayMs) {
        if (simulatedDelayMs < 0) {
            throw new IllegalArgumentException("задержка не может быть отрицательной");
        }
        this.simulatedDelayMs = simulatedDelayMs;
    }

    public ProcessingResult process(Document document) throws DocumentProcessingException, InterruptedException {
        if (simulatedDelayMs > 0) {
            Thread.sleep(simulatedDelayMs);
        }
        String content = document.content();
        if (content == null || content.isBlank()) {
            throw new DocumentProcessingException("документ пуст");
        }
        if (content.contains("#CORRUPTED")) {
            throw new DocumentProcessingException("документ повреждён (метка #CORRUPTED)");
        }
        return new ProcessingResult(document.id(), content.trim().split("\\s+").length);
    }
}
