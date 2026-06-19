package roomescape.payment.web;

/**
 * failUrl 콜백 바디. 사용자가 취소하면 orderId가 없을 수 있으므로 nullable이다(서비스에서 null 가드).
 */
public record PaymentFailRequestDto(String code, String message, String orderId) {
}
