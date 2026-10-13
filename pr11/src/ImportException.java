/** Проверяемая ошибка уровня файла: файл не открыт, пуст или имеет неверный заголовок. Импорт прекращается. */
public class ImportException extends Exception {
    public ImportException(String message) {
        super(message);
    }

    public ImportException(String message, Throwable cause) {
        super(message, cause);
    }
}
