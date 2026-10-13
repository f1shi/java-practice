/** Ошибка конкретной строки файла. */
public record ImportError(int lineNumber, String reason) {
    @Override
    public String toString() {
        return "строка " + lineNumber + ": " + reason;
    }
}
