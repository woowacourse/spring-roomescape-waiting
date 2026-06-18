package roomescape.domain;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public class Payment {

    private final Long id;
    private final OrderId orderId;
    private final Long reservationId;
    private final Long amount;
    private final String paymentKey;

    private static void validateId(final Long id) {
        if (id == null) {
            throw new IllegalArgumentException("결제 ID는 비워둘 수 없습니다.");
        }
    }

    private static void validateOrderId(final OrderId orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("주문 정보 ID가 비어있습니다.");
        }
    }

    private static void validatePaymentKey(final String paymentKey) {
        if (paymentKey == null || paymentKey.isBlank()) {
            throw new IllegalArgumentException("결제키는 비워둘 수 없습니다.");
        }
    }

    public static Payment prepare(final OrderId orderId, final Long reservationId, final Long amount) {
        validateOrderId(orderId);
        return new Payment(
                null,
                orderId,
                reservationId,
                amount,
                null
        );
    }

    public static Payment from(
            final Long id,
            final OrderId orderId,
            final Long reservationId,
            final Long amount,
            final String paymentKey
    ) {
        validateId(id);
        return new Payment(id, orderId, reservationId, amount, paymentKey);
    }

    public Payment confirm(final String paymentKey) {
        validatePaymentKey(paymentKey);
        return new Payment(
                this.id,
                this.orderId,
                this.reservationId,
                this.amount,
                paymentKey
        );
    }

    public Payment withId(final Long id) {
        validateId(id);
        return new Payment(
                id,
                this.orderId,
                this.reservationId,
                this.amount,
                this.paymentKey
        );
    }

    public String getOrderId() {
        return this.orderId.getId();
    }
}
