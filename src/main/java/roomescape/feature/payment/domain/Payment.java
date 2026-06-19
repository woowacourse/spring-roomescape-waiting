package roomescape.feature.payment.domain;

import lombok.Getter;

/**
 * 승인된(또는 확인 필요한) 결제의 식별 정보 기록. orderId 가 멱등키이며, 주문당 한 건만 존재한다.
 * 결제의 표시 상태(대기/확정/확인 필요)는 예약의 OrderStatus 가 보유하고, 이 기록은 orderId·paymentKey·금액을 담는다.
 */
@Getter
public class Payment {

    private final Long id;
    private final Long reservationId;
    private final String orderId;
    private final String paymentKey;
    private final long amount;

    private Payment(Long id, Long reservationId, String orderId, String paymentKey, long amount) {
        this.id = id;
        this.reservationId = reservationId;
        this.orderId = orderId;
        this.paymentKey = paymentKey;
        this.amount = amount;
    }

    public static Payment create(Long reservationId, String orderId, String paymentKey, long amount) {
        return new Payment(null, reservationId, orderId, paymentKey, amount);
    }

    public static Payment reconstruct(Long id, Long reservationId, String orderId, String paymentKey, long amount) {
        return new Payment(id, reservationId, orderId, paymentKey, amount);
    }
}
