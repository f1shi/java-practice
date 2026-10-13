import java.util.ArrayList;
import java.util.List;

/** Самопроверка: детерминированность счётчиков при разном размере пула и порядке завершения задач. */
public class SelfCheck {

    private static int passed;
    private static int failed;

    public static void main(String[] args) throws Exception {
        List<Document> docs = new ArrayList<>();
        int expectedWords = 0;
        for (int i = 1; i <= 50; i++) {
            if (i % 10 == 0) {
                docs.add(new Document("D" + i, i % 20 == 0 ? "" : "плохой #CORRUPTED"));
            } else {
                docs.add(new Document("D" + i, "слово ".repeat(i)));
                expectedWords += i;
            }
        }
        // 45 успешных, 5 ошибок, слов = сумма i для i не кратных 10; документов от 20 слов: 21..29, 31..39, 41..49 = 27
        for (int pool : new int[]{1, 3, 8}) {
            BatchResult r = new BatchProcessor(new DocumentProcessor(2), pool).processAll(docs);
            check("пул " + pool + ": " + r, r.getSuccessCount() == 45 && r.getErrorCount() == 5
                    && r.getTotalWords() == expectedWords && r.getLargeDocumentCount() == 27);
        }
        BatchResult seq = new BatchProcessor(new DocumentProcessor(0), 1).processSequentially(docs);
        check("последовательный режим даёт те же агрегаты", seq.getSuccessCount() == 45 && seq.getErrorCount() == 5
                && seq.getTotalWords() == expectedWords && seq.getLargeDocumentCount() == 27);

        // вариант 3: граница «объёмного» документа — 20 слов включительно
        BatchResult edge = new BatchProcessor(new DocumentProcessor(0), 2).processAll(List.of(
                new Document("W19", "а ".repeat(19)), new Document("W20", "а ".repeat(20)),
                new Document("W21", "а ".repeat(21)), new Document("BAD", "")));
        check("19 слов — нет, 20 и 21 — да, ошибочный не учитывается: " + edge, edge.getLargeDocumentCount() == 2);

        // ошибка одного документа не отменяет остальные
        BatchResult one = new BatchProcessor(new DocumentProcessor(0), 4).processAll(List.of(
                new Document("A", "раз два"), new Document("B", ""), new Document("C", "три")));
        check("ошибка B не мешает A и C", one.getSuccessCount() == 2 && one.getErrors().containsKey("B"));
        check("причина ошибки сохранена", one.getErrors().get("B").contains("документ пуст"));

        // повтор id
        BatchResult dup = new BatchProcessor(new DocumentProcessor(0), 2).processAll(List.of(
                new Document("X", "a b"), new Document("X", "c d e")));
        check("дубликат id: 1 успех, 1 ошибка", dup.getSuccessCount() == 1 && dup.getErrorCount() == 1);

        // границы размера пула: 1..8
        expect("пул 0", () -> new BatchProcessor(new DocumentProcessor(0), 0));
        check("пул 1 допустим", ok(() -> new BatchProcessor(new DocumentProcessor(0), 1)));
        check("пул 8 допустим", ok(() -> new BatchProcessor(new DocumentProcessor(0), 8)));
        expect("пул 9", () -> new BatchProcessor(new DocumentProcessor(0), 9));
        expect("список null", () -> {
            try {
                new BatchProcessor(new DocumentProcessor(0), 2).processAll(null);
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }
        });
        expect("пустой id документа", () -> new Document(" ", "x"));
        check("пустой список -> нули", new BatchProcessor(new DocumentProcessor(0), 2)
                .processAll(List.of()).getSuccessCount() == 0);

        // потоки пула завершаются: число активных потоков вернулось к исходному
        int before = Thread.activeCount();
        new BatchProcessor(new DocumentProcessor(1), 8).processAll(docs);
        Thread.sleep(100);
        check("после processAll потоки пула завершены (shutdown)", Thread.activeCount() <= before);

        System.out.printf("%nИтого: пройдено %d, не пройдено %d%n", passed, failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static boolean ok(Runnable action) {
        try {
            action.run();
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static void expect(String name, Runnable action) {
        try {
            action.run();
            check(name + " -> ожидалась ошибка", false);
        } catch (IllegalArgumentException e) {
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
