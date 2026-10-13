/** Общие проверки аргументов конструкторов. */
final class Validation {

    private Validation() {
    }

    static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " не может быть пустым");
        }
        return value.trim();
    }
}
