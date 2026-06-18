package roomescape.payment;

/**
 * 결제 주문 정보. 결제 전에 (reservation_id, order_id, amount)로 저장해 두고,
 * 승인 성공 시 payment_key 가 채워진다. payment_key 가 null 이면 아직 미승인 상태다.
 */
public record Payment(
        Long id,
        Long reservationId,
        String orderId,
        Long amount,
        String paymentKey
) {
}
