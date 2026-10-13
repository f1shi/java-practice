import java.nio.file.Path;

/** Самопроверка импорта: открытый файл, служебные файлы data/test и отдельные правила. */
public class SelfCheck {

    private static int passed;
    private static int failed;

    public static void main(String[] args) throws Exception {
        ClientRepository repo = new ClientRepository();
        ImportResult r = newImporter(repo).importFile(Path.of("data/sample.csv"));
        System.out.println(r);
        check("sample.csv: принято 4", r.getAccepted() == 4);
        check("sample.csv: отклонено 7, пустая строка пропущена", r.getRejected() == 7);
        check("sample.csv: 1 дубликат из 11 строк — без предупреждения", r.getDuplicates() == 1 && !r.isDuplicateWarning());
        check("ошибка содержит номер строки 4 (ИНН из 9 цифр)", r.getErrors().get(0).lineNumber() == 4);
        check("дубликат ИНН отклонён, сохранён первый клиент",
                repo.findByInn("7701234567").orElseThrow().name().equals("ООО Ромашка"));
        check("телефон нормализован до 10 цифр", repo.findByInn("7701234567").orElseThrow().phone().equals("9161234567"));

        // вариант 3: дубликатов больше 20 % строк — предупреждение, импорт не прерывается
        ImportResult reimport = newImporter(new ClientRepository()).importFile(Path.of("data/test/reimport.csv"));
        check("2 дубликата из 5 строк (40 %) -> предупреждение, 3 принято",
                reimport.isDuplicateWarning() && reimport.getAccepted() == 3 && reimport.getDuplicates() == 2);
        ImportResult exact = newImporter(new ClientRepository()).importFile(Path.of("data/test/duplicates_20pct.csv"));
        check("1 дубликат из 5 строк (ровно 20 %) -> без предупреждения",
                !exact.isDuplicateWarning() && exact.getDuplicates() == 1 && exact.getAccepted() == 4);

        fileError("файл не существует", "data/test/no_such_file.csv");
        fileError("пустой файл", "data/test/empty.csv");
        fileError("неверный заголовок", "data/test/bad_header.csv");
        ImportResult headerOnly = newImporter(new ClientRepository()).importFile(Path.of("data/test/header_only.csv"));
        check("только заголовок: 0 принято, 0 ошибок", headerOnly.getAccepted() == 0 && headerOnly.getRejected() == 0);

        ClientValidator v = new ClientValidator();
        check("ИНН 10 цифр", ClientValidator.isValidInn("7701234567"));
        check("ИНН 12 цифр", ClientValidator.isValidInn("770123456789"));
        check("ИНН 9 цифр — ошибка", !ClientValidator.isValidInn("770123456"));
        check("ИНН 11 цифр — ошибка", !ClientValidator.isValidInn("77012345678"));
        check("ИНН 13 цифр — ошибка", !ClientValidator.isValidInn("7701234567890"));
        check("ИНН с буквой — ошибка", !ClientValidator.isValidInn("77012345A7"));
        check("email a@b", ClientValidator.isValidEmail("a@b"));
        check("email без @", !ClientValidator.isValidEmail("ab.ru"));
        check("email @b", !ClientValidator.isValidEmail("@b.ru"));
        check("email a@", !ClientValidator.isValidEmail("a@"));
        check("email два @", !ClientValidator.isValidEmail("a@@b.ru"));
        check("телефон 10 цифр", "9161234567".equals(ClientValidator.normalizePhone("916 123-45-67")));
        check("телефон +7 и 11 цифр", "9161234567".equals(ClientValidator.normalizePhone("+7(916)1234567")));
        check("телефон 9 цифр — ошибка", ClientValidator.normalizePhone("916123456") == null);
        check("телефон с буквами — ошибка", ClientValidator.normalizePhone("916abc4567") == null);
        check("пустой телефон допустим (поле необязательное)",
                v.validate(new ClientDraft(2, "ООО", "7701234567", "a@b.ru", "")).isValid());
        check("несколько ошибок одной строки собираются вместе",
                v.validate(new ClientDraft(2, "", "1", "x", "12")).errors().size() == 4);

        System.out.printf("%nИтого: пройдено %d, не пройдено %d%n", passed, failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static ClientImporter newImporter(ClientRepository repo) {
        return new ClientImporter(new ClientCsvParser(), new ClientValidator(), repo);
    }

    private static void fileError(String name, String path) {
        try {
            newImporter(new ClientRepository()).importFile(Path.of(path));
            check(name + " -> ожидалась ImportException", false);
        } catch (ImportException e) {
            check(name + " -> " + e.getMessage(), true);
        }
    }

    private static void check(String name, boolean ok) {
        System.out.println((ok ? "[OK]   " : "[FAIL] ") + name);
        if (ok) {
            passed++;
        } else {
            failed++;
        }
    }
}
