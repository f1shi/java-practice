/** Сотрудник, направляемый в командировку. */
public record Employee(String id, String fullName) {
    public Employee {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("id сотрудника не может быть пустым");
        }
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("ФИО сотрудника не может быть пустым");
        }
    }
}
