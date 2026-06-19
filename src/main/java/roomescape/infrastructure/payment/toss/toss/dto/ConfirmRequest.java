package roomescape.infrastructure.payment.toss.toss.dto;

/**
 * Toss 결제 승인 요청 바디. 세 필드 모두 필수다.
 */
public record ConfirmRequest(String paymentKey, String orderId, Long amount) {

}
