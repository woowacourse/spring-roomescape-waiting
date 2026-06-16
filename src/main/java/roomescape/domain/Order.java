package roomescape.domain;

/**
 * 결제 전에 서버가 미리 저장해 두는 주문 정보. successUrl 의 amount 와 대조해 금액 위변조를 막는 기준값이다.
 */
public class Order {

    private final String orderId;
    private final Long amount;

    public Order(String orderId, Long amount) {
        this.orderId = orderId;
        this.amount = amount;
    }

    public String getOrderId() {
        return orderId;
    }

    public Long getAmount() {
        return amount;
    }

}
