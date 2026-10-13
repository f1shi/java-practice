import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Итог пакетного импорта: число принятых записей и список ошибок строк. */
public class ImportResult {

    private int accepted;
    private final List<ImportError> errors = new ArrayList<>();

    void addAccepted() {
        accepted++;
    }

    void addError(int lineNumber, String reason) {
        errors.add(new ImportError(lineNumber, reason));
    }

    public int getAccepted() {
        return accepted;
    }

    public int getRejected() {
        return errors.size();
    }

    public List<ImportError> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Принято: " + accepted + ", отклонено: " + errors.size());
        for (ImportError error : errors) {
            sb.append(System.lineSeparator()).append("  ").append(error);
        }
        return sb.toString();
    }
}
