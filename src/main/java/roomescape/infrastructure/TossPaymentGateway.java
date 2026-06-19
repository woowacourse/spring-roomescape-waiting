package roomescape.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import roomescape.domain.payment.PaymentGateway;
import roomescape.domain.payment.PaymentResult;
import roomescape.exception.PaymentException.AlreadyProcessedException;
import roomescape.exception.PaymentException.PaymentConfirmException;
import roomescape.exception.PaymentException.PaymentInternalException;

@Component
public class TossPaymentGateway implements PaymentGateway {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public TossPaymentGateway(RestClient.Builder restClientBuilder,
                              ObjectMapper objectMapper,
                              @Value("${toss.base-url}") String baseUrl,
                              @Value("${toss.secret-key}") String secretKey) {
        String encodedKey = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
        this.objectMapper = objectMapper;
    }

    @Retryable(retryFor = PaymentInternalException.class, backoff = @Backoff(delay = 50, multiplier = 2.0, random = true))
    @Override
    public PaymentResult confirm(String paymentKey, String orderId, long amount) {
        try {
            return Objects.requireNonNull(restClient.post().uri("/v1/payments/confirm")
                            .body(new TossConfirmRequest(paymentKey, orderId, amount))
                            .retrieve()
                            .onStatus(HttpStatusCode::isError, (request, response) -> handleConfirmError(response))
                            .body(TossConfirmResponse.class))
                    .toPaymentResult();
        } catch (AlreadyProcessedException e) {
            TossConfirmResponse confirmResponse = getPayment(paymentKey);
            if (!"DONE".equals(confirmResponse.status()) || confirmResponse.totalAmount() != amount) {
                throw new PaymentConfirmException("상태가 이상한 결제: " + confirmResponse.status());
            }
            return confirmResponse.toPaymentResult();
        }
    }

    private TossConfirmResponse getPayment(String paymentKey) {
        return restClient.get()
                .uri("/v1/payments/{paymentKey}", paymentKey)
                .retrieve()
                .body(TossConfirmResponse.class);
    }

    private void handleConfirmError(ClientHttpResponse response) throws IOException {
        TossErrorResponse error = parse(response);
        throw TossExceptionHandler.toDomainException(error.code(), error.message());
    }

    private TossErrorResponse parse(ClientHttpResponse response) throws IOException {
        return objectMapper.readValue(response.getBody(), TossErrorResponse.class);
    }

}
