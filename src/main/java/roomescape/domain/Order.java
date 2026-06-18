package roomescape.domain;

import roomescape.exception.ErrorType;
import roomescape.exception.RoomescapeException;

public class Order {
    private static final long DEFAULT_AMOUNT = 50000L; // 학습 목적상 고정값 사용

    private final Long id;
    private final OrderId orderId;
    private final Long reservationId;
    private final Long amount;
    private final PaymentStatus status;
    private final String paymentKey;

    public Order(Long id, OrderId orderId, Long reservationId, Long amount, PaymentStatus status, String paymentKey) {
        validate(orderId, reservationId, status);
        this.id = id;
        this.orderId = orderId;
        this.reservationId = reservationId;
        this.amount = amount;
        this.status = status;
        this.paymentKey = paymentKey;
    }

    public Order(Reservation reservation) {
        this(null, OrderId.generate(), extractReservationId(reservation), DEFAULT_AMOUNT, PaymentStatus.READY, null);
    }

    private static Long extractReservationId(Reservation reservation) {
        if (reservation == null) {
            throw new RoomescapeException(ErrorType.INVALID_DOMAIN, "주문 대상 예약은 필수입니다.");
        }
        if (reservation.getId() == null) {
            throw new RoomescapeException(ErrorType.INVALID_DOMAIN, "주문은 저장된 예약에 대해서만 생성할 수 있습니다.");
        }
        return reservation.getId();
    }

    private void validate(OrderId orderId, Long reservationId, PaymentStatus status) {
        if (orderId == null) {
            throw new RoomescapeException(ErrorType.INVALID_DOMAIN, "주문 번호는 필수입니다.");
        }
        if (reservationId == null) {
            throw new RoomescapeException(ErrorType.INVALID_DOMAIN, "예약 정보는 필수입니다.");
        }
        if (status == null) {
            throw new RoomescapeException(ErrorType.INVALID_DOMAIN, "결제 상태는 필수입니다.");
        }
    }

    public Order withId(Long id) {
        return new Order(id, orderId, reservationId, amount, status, paymentKey);
    }

    public Order withPayment(PaymentStatus status, String paymentKey) {
        return new Order(id, orderId, reservationId, amount, status, paymentKey);
    }

    public Long getId() {
        return id;
    }

    public OrderId getOrderId() {
        return orderId;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public Long getAmount() {
        return amount;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public String getPaymentKey() {
        return paymentKey;
    }
}
