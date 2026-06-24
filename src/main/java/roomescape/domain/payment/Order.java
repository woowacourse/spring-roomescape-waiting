package roomescape.domain.payment;

public class Order {

    private final Long id;
    private final String orderId;
    private final Long amount;
    private final Long reservationId;
    private final OrderStatus status;

    public Order(Long id, String orderId, Long amount, Long reservationId, OrderStatus status) {
        validateOrderId(orderId);
        validateAmount(amount);
        validateReservationId(reservationId);
        validateStatus(status);
        this.id = id;
        this.orderId = orderId;
        this.amount = amount;
        this.reservationId = reservationId;
        this.status = status;
    }

    public Order(String orderId, Long amount, Long reservationId) {
        this(null, orderId, amount, reservationId, OrderStatus.PENDING);
    }

    public Long getId() {
        return id;
    }

    public String getOrderId() {
        return orderId;
    }

    public Long getAmount() {
        return amount;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    private void validateOrderId(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("주문 번호는 필수입니다.");
        }
    }

    private void validateAmount(Long amount) {
        if (amount == null || amount < 0) {
            throw new IllegalArgumentException("주문 금액은 0 이상이어야 합니다.");
        }
    }

    private void validateReservationId(Long reservationId) {
        if (reservationId == null) {
            throw new IllegalArgumentException("예약 식별자는 필수입니다.");
        }
    }

    private void validateStatus(OrderStatus status) {
        if (status == null) {
            throw new IllegalArgumentException("주문 상태는 필수입니다.");
        }
    }
}
