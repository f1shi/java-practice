import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Фабрика выбирает канал по типу; отправкой и бизнес-логикой не занимается. */
public class ChannelFactory {

    private final Map<ChannelType, NotificationChannel> channels = new EnumMap<>(ChannelType.class);

    public ChannelFactory(List<NotificationChannel> available) {
        for (NotificationChannel channel : available) {
            if (channels.putIfAbsent(channel.type(), channel) != null) {
                throw new IllegalArgumentException("канал " + channel.type() + " зарегистрирован дважды");
            }
        }
    }

    /** Стандартный набор каналов. Новый канал = новый класс + одна строка здесь. */
    public static ChannelFactory withDefaultChannels() {
        return new ChannelFactory(List.of(new EmailChannel(), new SmsChannel(), new InternalChannel(), new PushChannel()));
    }

    public NotificationChannel get(ChannelType type) {
        NotificationChannel channel = channels.get(type);
        if (channel == null) {
            throw new IllegalArgumentException("канал " + type + " не подключён");
        }
        return channel;
    }
}
