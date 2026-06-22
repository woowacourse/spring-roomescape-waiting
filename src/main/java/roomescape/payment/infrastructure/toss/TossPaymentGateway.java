package roomescape.payment.infrastructure.toss;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentGateway;
import roomescape.payment.domain.PaymentResult;
import roomescape.payment.exception.PaymentApprovalUnknownException;
import roomescape.payment.exception.PaymentCommunicationException;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class TossPaymentGateway implements PaymentGateway {

    private static final String TOSS_CONFIRM_URI = "/v1/payments/confirm";
    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    private final RestClient tossRestClient;
    private final TossPaymentProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public PaymentResult confirm(final PaymentConfirmation confirmation) {
        validateSecretKey();

        try {
            final TossPaymentConfirmResponse response = tossRestClient.post()
                    .uri(TOSS_CONFIRM_URI)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader())
                    .header(IDEMPOTENCY_KEY_HEADER, confirmation.idempotencyKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new TossPaymentConfirmRequest(
                            confirmation.paymentKey(),
                            confirmation.orderId(),
                            confirmation.amount()
                    ))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, clientResponse) -> {
                        final TossErrorResponse errorResponse = objectMapper.readValue(
                                clientResponse.getBody(),
                                TossErrorResponse.class
                        );
                        throw TossPaymentErrorMapper.map(clientResponse.getStatusCode(), errorResponse);
                    })
                    .body(TossPaymentConfirmResponse.class);

            return toPaymentResult(response);
        } catch (ResourceAccessException exception) {
            throw mapResourceAccessException(exception);
        } catch (RestClientException exception) {
            if (hasCause(exception, SocketTimeoutException.class)) {
                throw mapRestClientException(exception);
            }
            if (exception.getCause() instanceof RuntimeException cause) {
                throw cause;
            }
            throw mapRestClientException(exception);
        }
    }

    @Override
    public String clientKey() {
        return properties.clientKey();
    }

    private void validateSecretKey() {
        if (properties.secretKey() == null || properties.secretKey().isBlank()) {
            throw new IllegalStateException("결제 시크릿 키가 설정되어 있지 않습니다.");
        }
    }

    private String authorizationHeader() {
        final byte[] credential = "%s:".formatted(properties.secretKey()).getBytes(StandardCharsets.UTF_8);

        return "Basic " + Base64.getEncoder().encodeToString(credential);
    }

    private PaymentResult toPaymentResult(
            final TossPaymentConfirmResponse response
    ) {
        if (response == null) {
            throw new IllegalStateException("결제 승인 서버와 통신하지 못했습니다.");
        }

        if (response.totalAmount() == null) {
            throw new IllegalStateException("결제 승인 응답에 금액 정보가 없습니다.");
        }

        return new PaymentResult(
                response.paymentKey(),
                response.orderId(),
                response.totalAmount()
        );
    }

    private RuntimeException mapResourceAccessException(final ResourceAccessException exception) {
        if (hasCause(exception, SocketTimeoutException.class)) {
            return new PaymentApprovalUnknownException(
                    "결제 승인 응답을 받지 못했습니다. 결제가 승인되었을 수 있으니 내 예약에서 상태를 확인해주세요.",
                    exception
            );
        }
        if (hasCause(exception, ConnectException.class)) {
            return new PaymentCommunicationException(
                    "결제 승인 서버에 연결하지 못했습니다. 잠시 후 다시 시도해주세요.",
                    exception
            );
        }

        return new PaymentCommunicationException(
                "결제 승인 서버와 통신하지 못했습니다. 잠시 후 다시 시도해주세요.",
                exception
        );
    }

    private RuntimeException mapRestClientException(final RestClientException exception) {
        if (hasCause(exception, SocketTimeoutException.class)) {
            return new PaymentApprovalUnknownException(
                    "결제 승인 응답을 받지 못했습니다. 결제가 승인되었을 수 있으니 내 예약에서 상태를 확인해주세요.",
                    exception
            );
        }

        return new PaymentCommunicationException(
                "결제 승인 서버와 통신하지 못했습니다. 잠시 후 다시 시도해주세요.",
                exception
        );
    }

    private boolean hasCause(final Throwable exception, final Class<? extends Throwable> causeType) {
        Throwable current = exception;
        while (current != null) {
            if (causeType.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }

        return false;
    }
}
