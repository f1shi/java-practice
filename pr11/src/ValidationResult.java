import java.util.List;

/** Результат проверки черновика: пустой список ошибок означает, что строка корректна. */
public record ValidationResult(List<String> errors) {

    public ValidationResult {
        errors = List.copyOf(errors);
    }

    public boolean isValid() {
        return errors.isEmpty();
    }

    public String message() {
        return String.join("; ", errors);
    }
}
