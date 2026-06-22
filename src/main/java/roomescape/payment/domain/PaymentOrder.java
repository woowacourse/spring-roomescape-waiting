package roomescape.payment.domain;

public record PaymentOrder(
        Long reservationId,
        String orderId,
        long amount,
        PaymentOrderStatus status,
        String paymentKey
) {
    public boolean hasSameAmount(long callbackAmount) {
        return amount == callbackAmount;
    }
}
