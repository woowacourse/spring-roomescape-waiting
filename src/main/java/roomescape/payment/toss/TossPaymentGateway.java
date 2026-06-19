package roomescape.payment.toss;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.RoomescapeException;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentGateway;
import roomescape.payment.PaymentResult;

@Component
public class TossPaymentGateway implements PaymentGateway {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final TossPaymentProperties properties;

    public TossPaymentGateway(RestClient.Builder restClientBuilder, ObjectMapper objectMapper, TossPaymentProperties properties) {
        this.restClient = restClientBuilder.build();
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    public PaymentResult confirm(PaymentConfirmation confirmation) {
        TossConfirmResponse response = restClient.post()
                .uri(properties.getConfirmUrl())
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.AUTHORIZATION, authorization())
                .body(TossConfirmRequest.from(confirmation))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, clientResponse) -> {
                    TossErrorResponse errorResponse = readErrorResponse(clientResponse.getBody().readAllBytes());
                    throw toDomainException(errorResponse);
                })
                .body(TossConfirmResponse.class);

        if (response == null) {
            throw new RoomescapeException(DomainErrorCode.PAYMENT_GATEWAY_ERROR, "결제 승인 응답을 확인할 수 없습니다.");
        }
        return response.toResult();
    }

    private String authorization() {
        if (properties.getSecretKey() == null || properties.getSecretKey().isBlank()) {
            throw new RoomescapeException(DomainErrorCode.PAYMENT_AUTHENTICATION_FAILED, "결제 승인 시크릿 키가 설정되지 않았습니다.");
        }
        String credential = properties.getSecretKey() + ":";
        String encoded = Base64.getEncoder().encodeToString(credential.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }

    private TossErrorResponse readErrorResponse(byte[] body) throws IOException {
        if (body.length == 0) {
            return new TossErrorResponse("UNKNOWN", "결제 승인 중 알 수 없는 오류가 발생했습니다.");
        }
        return objectMapper.readValue(body, TossErrorResponse.class);
    }

    private RoomescapeException toDomainException(TossErrorResponse errorResponse) {
        DomainErrorCode code = switch (errorResponse.code()) {
            case "ALREADY_PROCESSED_PAYMENT" -> DomainErrorCode.PAYMENT_ALREADY_PROCESSED;
            case "DUPLICATED_ORDER_ID", "NOT_FOUND_PAYMENT_SESSION", "INVALID_REQUEST" ->
                    DomainErrorCode.PAYMENT_INVALID_REQUEST;
            case "UNAUTHORIZED_KEY", "INVALID_API_KEY" -> DomainErrorCode.PAYMENT_AUTHENTICATION_FAILED;
            case "REJECT_CARD_PAYMENT" -> DomainErrorCode.PAYMENT_REJECTED;
            case "NOT_FOUND_PAYMENT" -> DomainErrorCode.PAYMENT_NOT_FOUND;
            case "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING" -> DomainErrorCode.PAYMENT_RETRYABLE;
            default -> DomainErrorCode.PAYMENT_GATEWAY_ERROR;
        };
        return new RoomescapeException(code, errorResponse.message());
    }

    private record TossConfirmRequest(
            String paymentKey,
            String orderId,
            int amount
    ) {

        private static TossConfirmRequest from(PaymentConfirmation confirmation) {
            return new TossConfirmRequest(
                    confirmation.paymentKey(),
                    confirmation.orderId(),
                    confirmation.amount()
            );
        }
    }

    private record TossConfirmResponse(
            String paymentKey,
            String orderId,
            int totalAmount,
            String status
    ) {

        private PaymentResult toResult() {
            return new PaymentResult(paymentKey, orderId, totalAmount, status);
        }
    }

    private record TossErrorResponse(
            String code,
            String message
    ) {
    }
}
