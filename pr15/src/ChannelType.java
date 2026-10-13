import java.util.Locale;

/** Тип канала уведомлений. */
public enum ChannelType {
    EMAIL, SMS, INTERNAL, PUSH;

    public static ChannelType parse(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("канал не указан");
        }
        try {
            return valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("неизвестный канал '" + raw.trim() + "'");
        }
    }
}
