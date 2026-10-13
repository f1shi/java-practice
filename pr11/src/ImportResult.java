import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Итог пакетного импорта: число принятых записей и список ошибок строк. */
public class ImportResult {

    /**
     * Индивидуальный вариант 3 — политика дубликатов (модификатор 20): повтор ИНН отклоняется как ошибка строки,
     * а если отклонённых дубликатов больше 20 % строк данных, результат получает предупреждение
     * «вероятно, файл уже импортировался» — импорт при этом не прерывается.
     */
    public static final int DUPLICATE_WARNING_PERCENT = 20;

    private int accepted;
    private int processedLines;
    private int duplicates;
    private final List<ImportError> errors = new ArrayList<>();

    void addAccepted() {
        accepted++;
    }

    void countLine() {
        processedLines++;
    }

    void addDuplicate(int lineNumber, String reason) {
        duplicates++;
        addError(lineNumber, reason);
    }

    public int getProcessedLines() {
        return processedLines;
    }

    public int getDuplicates() {
        return duplicates;
    }

    /** Строго больше 20 % строк данных — дубликаты (ровно 20 % предупреждения не даёт). */
    public boolean isDuplicateWarning() {
        return duplicates * 100 > processedLines * DUPLICATE_WARNING_PERCENT;
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
        StringBuilder sb = new StringBuilder("Принято: " + accepted + ", отклонено: " + errors.size()
                + " (из них дубликатов ИНН: " + duplicates + ")");
        if (isDuplicateWarning()) {
            sb.append(System.lineSeparator()).append("ПРЕДУПРЕЖДЕНИЕ: дубликатов больше ")
                    .append(DUPLICATE_WARNING_PERCENT).append(" % строк — вероятно, файл уже импортировался");
        }
        for (ImportError error : errors) {
            sb.append(System.lineSeparator()).append("  ").append(error);
        }
        return sb.toString();
    }
}
