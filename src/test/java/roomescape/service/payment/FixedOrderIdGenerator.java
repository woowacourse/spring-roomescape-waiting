package roomescape.service.payment;

public class FixedOrderIdGenerator implements OrderIdGenerator {

    private final String orderId;

    public FixedOrderIdGenerator(String orderId) {
        this.orderId = orderId;
    }

    @Override
    public String generate() {
        return orderId;
    }
}
