/** Документ для обработки. */
public record Document(String id, String content) {
    public Document {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id документа не может быть пустым");
        }
    }
}
