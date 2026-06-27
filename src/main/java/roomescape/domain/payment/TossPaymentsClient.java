package roomescape.domain.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.ResourceAccessException;
import roomescape.domain.payment.dto.PaymentConfirmRequest;
import roomescape.domain.payment.dto.PaymentConfirmResponse;
import roomescape.domain.payment.dto.PaymentErrorResponse;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.ratelimit.OutboundRateLimitInterceptor;
import roomescape.ratelimit.RetryAfterInterceptor;

@Component
public class TossPaymentsClient implements PaymentClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String secretKey;

    @Autowired
    public TossPaymentsClient(
        RestClient.Builder restClientBuilder,
        ObjectMapper objectMapper,
        @Value("${toss.payments.base-url:https://api.tosspayments.com}") String baseUrl,
        @Value("${toss.payments.secret-key:}") String secretKey,
        @Value("${toss.payments.connect-timeout}") Duration connectTimeout,
        @Value("${toss.payments.read-timeout}") Duration readTimeout,
        RetryAfterInterceptor retryAfterInterceptor,
        OutboundRateLimitInterceptor outboundRateLimitInterceptor
    ) {
        this(
            buildRestClient(
                restClientBuilder,
                baseUrl,
                connectTimeout,
                readTimeout,
                retryAfterInterceptor,
                outboundRateLimitInterceptor
            ),
            objectMapper,
            secretKey
        );
    }

    TossPaymentsClient(
        RestClient.Builder restClientBuilder,
        ObjectMapper objectMapper,
        String baseUrl,
        String secretKey,
        Duration connectTimeout,
        Duration readTimeout
    ) {
        this(
            buildRestClient(restClientBuilder, baseUrl, connectTimeout, readTimeout),
            objectMapper,
            secretKey
        );
    }

    TossPaymentsClient(RestClient restClient, ObjectMapper objectMapper, String secretKey) {
        this.restClient = restClient;
        this.objectMapper = objectMapper;
        this.secretKey = secretKey;
    }

    @Override
    public PaymentConfirmResponse confirm(PaymentConfirmRequest request, String idempotencyKey) {
        if (!StringUtils.hasText(secretKey)) {
            throw new RoomescapeException(ErrorCode.PAYMENT_SECRET_KEY_NOT_CONFIGURED);
        }

        try {
            return restClient.post()
                .uri("/v1/payments/confirm")
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader())
                .header("Idempotency-Key", idempotencyKey)
                .body(request)
                .retrieve()
                .body(PaymentConfirmResponse.class);
        } catch (RestClientResponseException exception) {
            PaymentErrorResponse errorResponse = parseErrorResponse(exception.getResponseBodyAsString());
            throw new PaymentException(
                exception.getStatusCode(),
                errorResponse.code(),
                errorResponse.message()
            );
        } catch (ResourceAccessException exception) {
            if (isReadTimeout(exception)) {
                throw new PaymentResultUnknownException();
            }
            throw new PaymentConnectionException();
        } catch (RestClientException exception) {
            if (hasCause(exception, SocketTimeoutException.class)) {
                throw new PaymentResultUnknownException();
            }
            throw new PaymentConnectionException();
        }
    }

    private String authorizationHeader() {
        String token = Base64.getEncoder()
            .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        return "Basic " + token;
    }

    private PaymentErrorResponse parseErrorResponse(String responseBody) {
        try {
            return objectMapper.readValue(responseBody, PaymentErrorResponse.class);
        } catch (JsonProcessingException exception) {
            return new PaymentErrorResponse(null, null);
        }
    }

    private boolean isReadTimeout(Throwable throwable) {
        Throwable rootCause = rootCause(throwable);
        if (!(rootCause instanceof SocketTimeoutException)) {
            return false;
        }
        String message = rootCause.getMessage();
        return message == null || !message.toLowerCase().contains("connect");
    }

    private boolean hasCause(Throwable throwable, Class<? extends Throwable> causeType) {
        return causeType.isInstance(rootCause(throwable));
    }

    private Throwable rootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }

    private static RestClient buildRestClient(
        RestClient.Builder restClientBuilder,
        String baseUrl,
        Duration connectTimeout,
        Duration readTimeout,
        ClientHttpRequestInterceptor... interceptors
    ) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);
        return restClientBuilder
            .baseUrl(baseUrl)
            .requestFactory(requestFactory)
            .requestInterceptors(list -> {
                for (ClientHttpRequestInterceptor interceptor : interceptors) {
                    list.add(interceptor);
                }
            })
            .build();
    }
}
