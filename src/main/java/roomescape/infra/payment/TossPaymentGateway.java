package roomescape.infra.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import roomescape.exception.server.PaymentUnavailableException;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentGateway;
import roomescape.payment.PaymentResult;

@Component
public class TossPaymentGateway implements PaymentGateway {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public TossPaymentGateway(
            @Value("${toss.base-url}") String baseUrl,
            @Value("${toss.secret-key:}") String secretKey,
            @Value("${toss.connect-timeout}") Duration connectTimeout,
            @Value("${toss.read-timeout}") Duration readTimeout,
            ObjectMapper objectMapper
    ) {
        String basic = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(connectTimeout);
        requestFactory.setReadTimeout(readTimeout);

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + basic)
                .requestFactory(requestFactory)
                .build();
        this.objectMapper = objectMapper;
    }

    @Override
    public PaymentResult confirm(PaymentConfirmation confirmation) {
        try {
            TossPaymentResponse response = restClient.post()
                    .uri("/v1/payments/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(TossConfirmRequest.from(confirmation))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, errorResponse) -> {
                        TossErrorResponse error = objectMapper.readValue(errorResponse.getBody(), TossErrorResponse.class);
                        throw TossPaymentException.of(errorResponse.getStatusCode(), error);
                    })
                    .body(TossPaymentResponse.class);
            return response.toResult();
        } catch (RestClientException e) {
            if (hasTimeoutOrConnectionCause(e)) {
                throw new PaymentUnavailableException();
            }
            throw e;
        }
    }

    private boolean hasTimeoutOrConnectionCause(Throwable e) {
        Throwable current = e;
        while (current != null) {
            if (current instanceof SocketTimeoutException || current instanceof ConnectException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
