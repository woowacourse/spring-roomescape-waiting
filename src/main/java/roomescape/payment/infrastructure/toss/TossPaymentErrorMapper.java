package roomescape.payment.infrastructure.toss;

import org.springframework.http.HttpStatusCode;
import roomescape.common.exception.ConflictException;
import roomescape.common.exception.NotFoundException;
import roomescape.common.exception.UnprocessableContentException;

final class TossPaymentErrorMapper {

    private TossPaymentErrorMapper() {
    }

    static RuntimeException map(final HttpStatusCode statusCode, final TossErrorResponse errorResponse) {
        final String code = errorResponse.code();
        final String message = messageFrom(errorResponse);

        return switch (code) {
            case "ALREADY_PROCESSED_PAYMENT" -> new ConflictException("이미 승인된 결제입니다.");
            case "DUPLICATED_ORDER_ID" -> new ConflictException(message);
            case "NOT_FOUND_PAYMENT_SESSION", "INVALID_REQUEST" -> new IllegalArgumentException(message);
            case "NOT_FOUND_PAYMENT" -> new NotFoundException(message);
            case "UNAUTHORIZED_KEY", "INVALID_API_KEY" ->
                    new IllegalStateException("결제 설정 오류가 발생했습니다.");
            case "REJECT_CARD_PAYMENT" -> new UnprocessableContentException(message);
            case "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING" ->
                    new IllegalStateException("결제 승인 중 일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
            default -> new IllegalStateException("결제 승인에 실패했습니다.");
        };
    }

    private static String messageFrom(final TossErrorResponse errorResponse) {
        if (errorResponse.message() == null || errorResponse.message().isBlank()) {
            return "결제 승인에 실패했습니다.";
        }

        return errorResponse.message();
    }
}
