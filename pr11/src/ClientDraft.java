/** Результат разбора строки до валидации: поля как есть, плюс номер строки файла. */
public record ClientDraft(int lineNumber, String name, String inn, String email, String phone) {

    public Client toClient() {
        return new Client(name, inn, email, ClientValidator.normalizePhone(phone));
    }
}
