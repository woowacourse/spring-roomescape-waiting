package roomescape.domain.payment;

import roomescape.exception.InvalidDomainException;

public class PaymentOrder {

    private static final String ORDER_ID_PATTERN = "[A-Za-z0-9_-]{6,64}";

    private final Long id;
    private final Long reservationId;
    private final String orderId;
    private final long amount;

    public PaymentOrder(Long id, Long reservationId, String orderId, long amount) {
        validate(reservationId, orderId, amount);
        this.id = id;
        this.reservationId = reservationId;
        this.orderId = orderId;
        this.amount = amount;
    }

    private void validate(Long reservationId, String orderId, long amount) {
        if (reservationId == null) {
            throw new InvalidDomainException("예약 id는 필수입니다.");
        }
        if (orderId == null || !orderId.matches(ORDER_ID_PATTERN)) {
            throw new InvalidDomainException("주문 id는 6~64자의 영숫자, '-', '_'만 사용할 수 있습니다.");
        }
        if (amount <= 0) {
            throw new InvalidDomainException("결제 금액은 양수여야 합니다.");
        }
    }

    public PaymentOrder withId(Long id) {
        if (this.id != null) {
            throw new InvalidDomainException("이미 id가 존재하는 도메인입니다. 도메인 id는 생성 이후 수정될 수 없습니다.");
        }
        return new PaymentOrder(id, reservationId, orderId, amount);
    }

    public Long getId() {
        return id;
    }

    public Long getReservationId() {
        return reservationId;
    }

    public String getOrderId() {
        return orderId;
    }

    public long getAmount() {
        return amount;
    }
}
