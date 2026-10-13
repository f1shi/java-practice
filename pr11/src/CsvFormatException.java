/** Строка CSV не соответствует структуре (неверное число полей). Ошибка одной строки, импорт продолжается. */
public class CsvFormatException extends RuntimeException {
    public CsvFormatException(String message) {
        super(message);
    }
}
