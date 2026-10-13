/** Конечный набор состояний заявки на командировку. REJECTED, CANCELLED и COMPLETED — финальные. */
public enum TripStatus {
    DRAFT, SUBMITTED, APPROVED, REJECTED, CANCELLED, COMPLETED
}
