/** Разбор одной строки "name;inn;email;phone". Только структура, без бизнес-проверок. */
public class ClientCsvParser {

    public static final String HEADER = "name;inn;email;phone";
    static final int FIELD_COUNT = 4;

    public ClientDraft parse(String line, int lineNumber) {
        String[] p = line.split(";", -1);
        if (p.length != FIELD_COUNT) {
            throw new CsvFormatException("ожидалось " + FIELD_COUNT + " поля, получено " + p.length);
        }
        return new ClientDraft(lineNumber, p[0].trim(), p[1].trim(), p[2].trim(), p[3].trim());
    }

    public boolean isHeader(String line) {
        return HEADER.equalsIgnoreCase(line.strip().replace("﻿", ""));
    }
}
