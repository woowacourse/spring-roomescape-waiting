package roomescape.infrastructure.payment.client.dto;

/**
 * Toss 결제 API 에러 응답.
 * {"code": "REJECT_CARD_PAYMENT", "message": "..."}
 */
public record TossErrorResponse(
        String code,
        String message
) {
}
