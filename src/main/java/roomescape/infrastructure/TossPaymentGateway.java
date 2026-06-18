package roomescape.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import roomescape.domain.payment.PaymentGateway;
import roomescape.domain.payment.PaymentResult;
import roomescape.exception.PaymentException.AlreadyProcessedException;
import roomescape.exception.PaymentException.PaymentConfirmException;
import roomescape.exception.PaymentException.PaymentInternalException;
import roomescape.exception.PaymentException.PaymentRateLimitException;
import roomescape.exception.PaymentException.PaymentResultUnknownException;

@Component
public class TossPaymentGateway implements PaymentGateway {

    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public TossPaymentGateway(RestClient.Builder restClientBuilder,
                              ClientHttpRequestFactory tossClientHttpRequestFactory,
                              ObjectMapper objectMapper,
                              List<ClientHttpRequestInterceptor> interceptors,
                              @Value("${toss.base-url}") String baseUrl,
                              @Value("${toss.secret-key}") String secretKey) {
        String encodedKey = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

        RestClient.Builder builder = restClientBuilder
                .requestFactory(tossClientHttpRequestFactory)
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        interceptors.forEach(builder::requestInterceptor);
        this.restClient = builder.build();
        this.objectMapper = objectMapper;
    }

    @Retryable(
            retryFor = {PaymentInternalException.class, ResourceAccessException.class},
            backoff = @Backoff(delay = 50, multiplier = 2.0, random = true))
    @Override
    public PaymentResult confirm(String paymentKey, String orderId, long amount) {
        try {
            return Objects.requireNonNull(restClient.post().uri("/v1/payments/confirm")
                            .header(IDEMPOTENCY_KEY_HEADER, orderId)
                            .body(new TossConfirmRequest(paymentKey, orderId, amount))
                            .retrieve()
                            .onStatus(HttpStatusCode::isError, (request, response) -> handleConfirmError(response))
                            .body(TossConfirmResponse.class))
                    .toPaymentResult();
        } catch (AlreadyProcessedException e) {
            return resolveByStatus(paymentKey, amount);
        }
    }

    @Recover
    public PaymentResult recoverConfirm(Throwable e, String paymentKey, String orderId, long amount) {
        if (e instanceof PaymentInternalException internal) {
            throw internal;
        }
        if (e instanceof PaymentRateLimitException rateLimited) {
            throw rateLimited;
        }
        try {
            return resolveByStatus(paymentKey, amount);
        } catch (RestClientException lookupFailure) {
            throw new PaymentResultUnknownException("결제 승인 결과를 확인하지 못했습니다. 잠시 후 다시 확인해 주세요.");
        }
    }

    private PaymentResult resolveByStatus(String paymentKey, long amount) {
        TossConfirmResponse confirmResponse = getPayment(paymentKey);
        if (!"DONE".equals(confirmResponse.status()) || confirmResponse.totalAmount() != amount) {
            throw new PaymentConfirmException("상태가 이상한 결제: " + confirmResponse.status());
        }
        return confirmResponse.toPaymentResult();
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
