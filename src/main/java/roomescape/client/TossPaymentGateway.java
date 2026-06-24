package roomescape.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import roomescape.client.dto.ConfirmRequest;
import roomescape.client.dto.TossErrorResponse;
import roomescape.client.dto.TossPaymentResponse;
import roomescape.common.exception.RoomEscapeException;
import roomescape.common.exception.code.PaymentErrorCode;
import roomescape.domain.PaymentConfirmation;
import roomescape.domain.PaymentGateway;
import roomescape.domain.PaymentResult;
import roomescape.domain.PaymentStatus;

@Component
public class TossPaymentGateway implements PaymentGateway {

    private final RestClient tossRestClient;
    private final ObjectMapper objectMapper;

    public TossPaymentGateway(RestClient tossRestClient, ObjectMapper objectMapper) {
        this.tossRestClient = tossRestClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public PaymentResult confirm(PaymentConfirmation confirmation) {
        ConfirmRequest request = new ConfirmRequest(confirmation.paymentKey(), confirmation.orderId(),
                confirmation.amount());
        try {
            TossPaymentResponse response = tossRestClient.post()
                    .uri("/v1/payments/confirm")
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        TossErrorResponse error = objectMapper.readValue(res.getBody(), TossErrorResponse.class);
                        if ("ALREADY_PROCESSED_PAYMENT".equals(error.code())) {
                            throw new AlreadyProcessedPaymentException();
                        }
                        throw toException(error);
                    })
                    .body(TossPaymentResponse.class);

            return new PaymentResult(request.paymentKey(), request.orderId(), PaymentStatus.from(response.status()),
                    response.totalAmount());
        } catch (AlreadyProcessedPaymentException exception) {
            return new PaymentResult(request.paymentKey(), request.orderId(), PaymentStatus.DONE, request.amount());
        }
    }

    private RoomEscapeException toException(TossErrorResponse error) {
        PaymentErrorCode code = switch (error.code()) {
            case "ALREADY_PROCESSED_PAYMENT" -> PaymentErrorCode.ALREADY_PROCESSED;
            case "DUPLICATED_ORDER_ID"       -> PaymentErrorCode.DUPLICATED_ORDER;
            case "NOT_FOUND_PAYMENT_SESSION" -> PaymentErrorCode.SESSION_EXPIRED;
            case "INVALID_REQUEST"           -> PaymentErrorCode.INVALID_REQUEST;
            case "UNAUTHORIZED_KEY",
                 "INVALID_API_KEY"           -> PaymentErrorCode.GATEWAY_CONFIG_ERROR;
            case "REJECT_CARD_PAYMENT"       -> PaymentErrorCode.CARD_REJECTED;
            case "NOT_FOUND_PAYMENT"         -> PaymentErrorCode.NOT_FOUND;
            case "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING" -> PaymentErrorCode.GATEWAY_INTERNAL_ERROR;
            default                          -> PaymentErrorCode.UNKNOWN;
        };
        return new RoomEscapeException(code);
    }

    private static class AlreadyProcessedPaymentException extends RuntimeException {}
}
