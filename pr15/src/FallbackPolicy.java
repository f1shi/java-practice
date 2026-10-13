/**
 * Индивидуальный вариант 3 — политика резервного канала (модификатор 20).
 * Если основной канал отклонил запрос и в запросе указан резервный адресат, уведомление отправляется
 * через резервный канал (INTERNAL). Число резервных отправок ограничено 20 на экземпляр сервиса:
 * при массовом сбое основного канала внутренний ящик не заваливается сообщениями.
 */
public class FallbackPolicy {

    public static final ChannelType DEFAULT_CHANNEL = ChannelType.INTERNAL;
    /** Параметр варианта 3: максимум резервных отправок. */
    public static final int DEFAULT_LIMIT = 20;

    private final ChannelType channel;
    private final int limit;
    private int used;

    public FallbackPolicy(ChannelType channel, int limit) {
        if (channel == null) {
            throw new IllegalArgumentException("резервный канал не указан");
        }
        if (limit < 0) {
            throw new IllegalArgumentException("лимит резервных отправок не может быть отрицательным");
        }
        this.channel = channel;
        this.limit = limit;
    }

    public static FallbackPolicy defaultPolicy() {
        return new FallbackPolicy(DEFAULT_CHANNEL, DEFAULT_LIMIT);
    }

    public ChannelType channel() {
        return channel;
    }

    /** Занимает одну резервную отправку; false, если лимит исчерпан. */
    boolean tryAcquire() {
        if (used >= limit) {
            return false;
        }
        used++;
        return true;
    }

    public int used() {
        return used;
    }

    public int limit() {
        return limit;
    }
}
