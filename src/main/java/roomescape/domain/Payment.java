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
    private final PaymentStatus status;

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
                null,
                PaymentStatus.READY
        );
    }

    public static Payment from(
            final Long id,
            final OrderId orderId,
            final Long reservationId,
            final Long amount,
            final String paymentKey,
            final PaymentStatus status
    ) {
        validateId(id);
        return new Payment(id, orderId, reservationId, amount, paymentKey, status);
    }

    public Payment confirm(final String paymentKey) {
        validatePaymentKey(paymentKey);
        return new Payment(
                this.id,
                this.orderId,
                this.reservationId,
                this.amount,
                paymentKey,
                PaymentStatus.DONE
        );
    }

    public Payment abort(final String paymentKey) {
        validatePaymentKey(paymentKey);
        return new Payment(
                this.id,
                this.orderId,
                this.reservationId,
                this.amount,
                paymentKey,
                PaymentStatus.ABORTED
        );
    }

    public Payment withId(final Long id) {
        validateId(id);
        return new Payment(
                id,
                this.orderId,
                this.reservationId,
                this.amount,
                this.paymentKey,
                this.status
        );
    }

    public boolean isSameAmount(final Long amount) {
        return this.amount.equals(amount);
    }
}
