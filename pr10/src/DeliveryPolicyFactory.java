import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/** Фабрика отвечает только за выбор реализации по типу; формул расчёта здесь нет. */
public class DeliveryPolicyFactory {

    private final Map<DeliveryType, DeliveryPolicy> policies = new EnumMap<>(DeliveryType.class);

    public DeliveryPolicyFactory(List<DeliveryPolicy> available) {
        for (DeliveryPolicy policy : available) {
            if (policies.putIfAbsent(policy.type(), policy) != null) {
                throw new IllegalArgumentException("для " + policy.type() + " зарегистрировано две политики");
            }
        }
    }

    /** Стандартный набор политик приложения. Новый способ = новый класс + одна строка здесь. */
    public static DeliveryPolicyFactory withDefaultPolicies() {
        return new DeliveryPolicyFactory(List.of(
                new PickupPolicy(),
                new StandardCourierPolicy(),
                new ExpressPolicy(),
                new RegionalPolicy()));
    }

    public DeliveryPolicy get(DeliveryType type) {
        DeliveryPolicy policy = policies.get(type);
        if (policy == null) {
            throw new IllegalArgumentException("для способа " + type + " нет реализации DeliveryPolicy");
        }
        return policy;
    }
}
