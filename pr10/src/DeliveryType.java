import java.util.Locale;

/** Способ доставки. Строка превращается в enum один раз — на входе в систему. */
public enum DeliveryType {
    PICKUP, STANDARD, EXPRESS, PARCEL_LOCKER;

    public static DeliveryType parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("способ доставки не указан");
        }
        try {
            return valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("неизвестный способ доставки '" + raw.trim() + "'");
        }
    }
}
