package roomescape.payment.infra.toss;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import roomescape.global.ratelimit.TokenBucketRateLimiter;
import roomescape.payment.config.OutboundRateLimitProperties;
import roomescape.payment.config.PaymentProperties;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentGateway;
import roomescape.payment.domain.PaymentResult;
import roomescape.payment.domain.exception.PaymentGatewayException;
import roomescape.payment.domain.exception.PaymentKeyConfigurationException;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class TossPaymentGateway implements PaymentGateway {
    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final PaymentProperties paymentProperties;

    public TossPaymentGateway(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            PaymentProperties paymentProperties,
            OutboundRateLimitProperties outboundRateLimitProperties
    ) {
        this.restClient = restClientBuilder
                .baseUrl(paymentProperties.toss().baseUrl())
                .requestFactory(tossRequestFactory(paymentProperties.toss()))
                .requestInterceptor(new OutboundRateLimitInterceptor(outboundRateLimiter(outboundRateLimitProperties)))
                .requestInterceptor(new RetryAfterInterceptor(
                        paymentProperties.toss().maxAttempts(),
                        paymentProperties.toss().retryAfterFallbackDelay()
                ))
                .build();
        this.objectMapper = objectMapper;
        this.paymentProperties = paymentProperties;
    }

    @Override
    public PaymentResult confirm(PaymentConfirmation confirmation) {
        TossPaymentConfirmResponse response;
        try {
            response = restClient.post()
                    .uri("/v1/payments/confirm")
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
                        if (clientResponse.getStatusCode().isSameCodeAs(HttpStatus.TOO_MANY_REQUESTS)) {
                            throw TossPaymentExceptionMapper.map(clientResponse.getStatusCode(), null);
                        }

                        TossErrorResponse errorResponse = readErrorResponse(clientResponse.getBody());
                        throw TossPaymentExceptionMapper.map(clientResponse.getStatusCode(), errorResponse);
                    })
                    .body(TossPaymentConfirmResponse.class);
        } catch (RestClientException exception) {
            throw TossPaymentExceptionMapper.map(exception);
        }

        if (response == null) {
            throw new PaymentGatewayException("결제 승인 응답이 비어 있습니다.");
        }

        return new PaymentResult(response.paymentKey(), response.orderId(), response.totalAmount());
    }

    private TokenBucketRateLimiter outboundRateLimiter(OutboundRateLimitProperties properties) {
        return new TokenBucketRateLimiter(properties.capacity(), properties.refillPerSec(), System::nanoTime);
    }

    private SimpleClientHttpRequestFactory tossRequestFactory(PaymentProperties.Toss tossProperties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(tossProperties.connectTimeout());
        factory.setReadTimeout(tossProperties.readTimeout());
        return factory;
    }

    private TossErrorResponse readErrorResponse(java.io.InputStream body) {
        try {
            return objectMapper.readValue(body, TossErrorResponse.class);
        } catch (SocketTimeoutException exception) {
            throw TossPaymentExceptionMapper.pendingForTimeout(exception);
        } catch (IOException exception) {
            throw new PaymentGatewayException("결제 승인 실패 응답을 해석하지 못했습니다.");
        }
    }

    private String authorizationHeader() {
        String secretKey = paymentProperties.toss().secretKey();
        if (secretKey == null || secretKey.isBlank()) {
            throw new PaymentKeyConfigurationException("Toss Payments 시크릿 키가 설정되지 않았습니다.");
        }

        byte[] token = (secretKey + ":").getBytes(StandardCharsets.UTF_8);
        return "Basic " + Base64.getEncoder().encodeToString(token);
    }
}
