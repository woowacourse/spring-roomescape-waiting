package roomescape.payment.toss.dto;

/**
 * 토스 결제 취소(환불) 요청 바디. 어댑터 안에서만 사용한다.
 */
public record TossCancelRequest(String cancelReason) {
}
