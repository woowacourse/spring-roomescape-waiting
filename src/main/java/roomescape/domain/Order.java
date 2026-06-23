package roomescape.domain;

/**
 * 결제 전에 서버가 미리 저장해 두는 주문 정보. successUrl 의 amount 와 대조해 금액 위변조를 막는 기준값이다.
 */
public class Order {

    private final String orderId;
    private final String orderName;
    private final Long amount;
    private final Reservation reservation;

    public Order(
            String orderId,
            String orderName,
            Long amount,
            Reservation reservation
    ) {
        this.orderId = orderId;
        this.orderName = orderName;
        this.amount = amount;
        this.reservation = reservation;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getOrderName() {
        return orderName;
    }

    public Long getAmount() {
        return amount;
    }

    public Reservation getReservation() {
        return reservation;
    }

}
