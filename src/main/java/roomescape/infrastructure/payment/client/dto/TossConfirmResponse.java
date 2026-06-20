package roomescape.infrastructure.payment.client.dto;

/**
 * Toss 결제 승인 API 성공 응답.
 * 실제 응답 필드는 훨씬 많지만, 현재 서비스에서 필요한 paymentKey만 매핑한다.
 */
public record TossConfirmResponse(
        String paymentKey
) {
}
