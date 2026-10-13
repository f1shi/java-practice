import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Пакетный импорт клиентов. Ошибка файла -> ImportException (импорт прекращается);
 * ошибка строки -> запись в ImportResult и переход к следующей строке.
 */
public class ClientImporter {

    private final ClientCsvParser parser;
    private final ClientValidator validator;
    private final ClientRepository repository;

    public ClientImporter(ClientCsvParser parser, ClientValidator validator, ClientRepository repository) {
        this.parser = parser;
        this.validator = validator;
        this.repository = repository;
    }

    public ImportResult importFile(Path file) throws ImportException {
        ImportResult result = new ImportResult();
        try (BufferedReader reader = Files.newBufferedReader(file, StandardCharsets.UTF_8)) {
            String header = reader.readLine();
            if (header == null) {
                throw new ImportException("файл " + file + " пуст: нет строки заголовка");
            }
            if (!parser.isHeader(header)) {
                throw new ImportException("неверный заголовок '" + header + "', ожидался '" + ClientCsvParser.HEADER + "'");
            }
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (!line.isBlank()) {
                    importLine(line, lineNumber, result);
                }
            }
        } catch (IOException e) {
            throw new ImportException("не удалось прочитать файл " + file + ": " + e.getMessage(), e);
        }
        return result;
    }

    /** Обработка одной строки; её ошибки не выходят за пределы метода. */
    private void importLine(String line, int lineNumber, ImportResult result) {
        result.countLine();
        ClientDraft draft;
        try {
            draft = parser.parse(line, lineNumber);
        } catch (CsvFormatException e) {
            result.addError(lineNumber, e.getMessage());
            return;
        }
        ValidationResult validation = validator.validate(draft);
        if (!validation.isValid()) {
            result.addError(lineNumber, validation.message());
            return;
        }
        Optional<Client> existing = repository.findByInn(draft.inn());
        if (existing.isPresent()) {
            // политика дубликатов (вариант 3): первая запись остаётся, повтор отклоняется и учитывается отдельно
            result.addDuplicate(lineNumber, "дубликат ИНН " + draft.inn() + " (уже импортирован клиент '"
                    + existing.get().name() + "')");
            return;
        }
        repository.save(draft.toClient());
        result.addAccepted();
    }
}
