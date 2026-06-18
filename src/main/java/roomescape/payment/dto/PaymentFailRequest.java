package roomescape.payment.dto;

/**
 * failUrl 콜백 정보. 사용자가 결제를 취소(PAY_PROCESS_CANCELED)한 경우 orderId가 없을 수 있어 모두 nullable이다.
 */
public record PaymentFailRequest(String code, String message, String orderId) {
}
