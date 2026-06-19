package roomescape.payment.toss;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

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

    public TossPaymentGateway(RestClient tossRestClient, ObjectMapper objectMapper, TossPaymentProperties properties) {
        this.restClient = tossRestClient;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    @Override
    public PaymentResult confirm(PaymentConfirmation confirmation) {
        TossConfirmResponse response;
        try {
            response = restClient.post()
                    .uri(properties.getConfirmUrl())
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, authorization())
                    .header("Idempotency-Key", confirmation.idempotencyKey())
                    .body(TossConfirmRequest.from(confirmation))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, clientResponse) -> {
                        TossErrorResponse errorResponse = readErrorResponse(clientResponse.getBody().readAllBytes());
                        throw toDomainException(errorResponse);
                    })
                    .body(TossConfirmResponse.class);
        } catch (RestClientException e) {
            throw toNetworkException(e);
        }

        if (response == null) {
            throw new RoomescapeException(DomainErrorCode.PAYMENT_GATEWAY_ERROR, "결제 승인 응답을 확인할 수 없습니다.");
        }
        return response.toResult();
    }

    private RoomescapeException toNetworkException(RestClientException exception) {
        Throwable rootCause = rootCause(exception);
        if (rootCause instanceof ConnectException) {
            return new RoomescapeException(DomainErrorCode.PAYMENT_RETRYABLE, "결제 승인 서버에 연결할 수 없습니다. 잠시 후 다시 시도해주세요.");
        }
        if (rootCause instanceof SocketTimeoutException) {
            if (isReadTimeout(rootCause)) {
                return new RoomescapeException(DomainErrorCode.PAYMENT_CONFIRM_UNKNOWN, "결제 승인 응답을 받지 못했습니다. 결제 결과 확인이 필요합니다.");
            }
            return new RoomescapeException(DomainErrorCode.PAYMENT_RETRYABLE, "결제 승인 서버 연결 시간이 초과되었습니다. 잠시 후 다시 시도해주세요.");
        }
        return new RoomescapeException(DomainErrorCode.PAYMENT_GATEWAY_ERROR, "결제 승인 서버 응답을 처리할 수 없습니다.");
    }

    private boolean isReadTimeout(Throwable throwable) {
        return throwable.getMessage() != null && throwable.getMessage().toLowerCase().contains("read");
    }

    private Throwable rootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current;
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
