/** Зафиксированная складская операция (приход или выдача). Неизменяемая запись. */
public record StockMovement(String itemCode, MovementType type, int quantity) {
}
