import java.util.Objects;

/** Сотрудник, для которого формируется заказ. Неизменяемый объект. */
public final class Employee {
    private final String id;
    private final String fullName;

    public Employee(String id, String fullName) {
        this.id = Validation.requireText(id, "id сотрудника");
        this.fullName = Validation.requireText(fullName, "ФИО сотрудника");
    }

    public String getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Employee other && id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return fullName + " (" + id + ")";
    }
}
