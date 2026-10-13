import java.util.ArrayList;
import java.util.List;

/** Бизнес-проверки полей клиента. Возвращает все найденные ошибки строки, а не только первую. */
public class ClientValidator {

    /** Индивидуальный вариант 1: телефон — обязательное поле, ровно 10 цифр номера (без кода страны). */
    static final int PHONE_DIGITS = 10;

    public ValidationResult validate(ClientDraft draft) {
        List<String> errors = new ArrayList<>();
        if (draft.name().isEmpty()) {
            errors.add("наименование не заполнено");
        }
        if (!isValidInn(draft.inn())) {
            errors.add("ИНН должен содержать 10 или 12 цифр: '" + draft.inn() + "'");
        }
        if (!isValidEmail(draft.email())) {
            errors.add("некорректный email: '" + draft.email() + "'");
        }
        if (draft.phone().isEmpty()) {
            errors.add("телефон не заполнен (обязательное поле)");
        } else if (normalizePhone(draft.phone()) == null) {
            errors.add("телефон должен содержать " + PHONE_DIGITS + " цифр номера: '" + draft.phone() + "'");
        }
        return new ValidationResult(errors);
    }

    static boolean isValidInn(String inn) {
        return (inn.length() == 10 || inn.length() == 12) && allDigits(inn);
    }

    /** Ровно один '@', непустые части слева и справа. */
    static boolean isValidEmail(String email) {
        int at = email.indexOf('@');
        return at > 0 && at == email.lastIndexOf('@') && at < email.length() - 1;
    }

    /**
     * Оставляет только цифры; "+7" или "8" в начале 11-значного номера считается кодом страны.
     * Возвращает 10 цифр номера или null, если номер некорректен.
     */
    static String normalizePhone(String phone) {
        String digits = phone.replaceAll("[\\s()+-]", "");
        if (!allDigits(digits)) {
            return null;
        }
        if (digits.length() == PHONE_DIGITS + 1 && (digits.charAt(0) == '7' || digits.charAt(0) == '8')) {
            digits = digits.substring(1);
        }
        return digits.length() == PHONE_DIGITS ? digits : null;
    }

    private static boolean allDigits(String s) {
        if (s.isEmpty()) {
            return false;
        }
        for (int i = 0; i < s.length(); i++) {
            if (!Character.isDigit(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }
}
