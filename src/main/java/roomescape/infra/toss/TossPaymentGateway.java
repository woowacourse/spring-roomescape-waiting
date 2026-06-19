package roomescape.infra.toss;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import roomescape.domain.payment.PaymentConfirmation;
import roomescape.domain.payment.PaymentGateway;
import roomescape.domain.payment.PaymentResult;
import roomescape.global.exception.CustomException;
import roomescape.global.exception.ErrorCode;
import roomescape.infra.toss.dto.TossErrorResponse;
import roomescape.infra.toss.dto.TossPaymentRequest;
import roomescape.infra.toss.dto.TossPaymentResponse;

@Component
public class TossPaymentGateway implements PaymentGateway {

    private static final String CONFIRM_PATH = "/v1/payments/confirm";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public TossPaymentGateway(RestClient tossRestClient, ObjectMapper objectMapper) {
        this.restClient = tossRestClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public PaymentResult confirm(PaymentConfirmation confirmation) {
        TossPaymentRequest request = new TossPaymentRequest(
                confirmation.paymentKey(),
                confirmation.orderId(),
                confirmation.amount()
        );

        TossPaymentResponse response = restClient.post()
                .uri(CONFIRM_PATH)
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (statusCode, clientResponse) -> {
                    TossErrorResponse error = objectMapper.readValue(
                            clientResponse.getBody(), TossErrorResponse.class
                    );
                    throw new CustomException(mapToErrorCode(error));
                })
                .body(TossPaymentResponse.class);

        if (response == null) {
            throw new CustomException(ErrorCode.PAYMENT_UNKNOWN_ERROR);
        }
        return new PaymentResult(response.paymentKey(), response.orderId(), response.totalAmount());
    }

    private ErrorCode mapToErrorCode(TossErrorResponse error) {
        if (error == null || error.code() == null) {
            return ErrorCode.PAYMENT_UNKNOWN_ERROR;
        }
        return switch (error.code()) {
            case "ALREADY_PROCESSED_PAYMENT" -> ErrorCode.PAYMENT_ALREADY_PROCESSED;
            case "CARD_REJECTED", "EXCEED_MAX_DAILY_PAYMENT_COUNT",
                 "EXCEED_MAX_PAYMENT_AMOUNT" -> ErrorCode.PAYMENT_CARD_REJECTED;
            case "UNAUTHORIZED_KEY", "INVALID_API_SECRET_KEY" -> ErrorCode.PAYMENT_UNAUTHORIZED_KEY;
            case "NOT_FOUND_PAYMENT", "NOT_FOUND_PAYMENT_SESSION" -> ErrorCode.PAYMENT_NOT_FOUND;
            case "TOSS_PAYMENTS_ERROR" -> ErrorCode.PAYMENT_TOSS_INTERNAL_ERROR;
            default -> ErrorCode.PAYMENT_UNKNOWN_ERROR;
        };
    }
}
