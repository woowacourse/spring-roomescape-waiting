package roomescape.domain.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import roomescape.domain.payment.dto.PaymentConfirmRequest;
import roomescape.domain.payment.dto.PaymentConfirmResponse;
import roomescape.domain.payment.dto.PaymentErrorResponse;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;

@Component
public class TossPaymentsClient implements PaymentClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String secretKey;

    public TossPaymentsClient(
        RestClient.Builder restClientBuilder,
        ObjectMapper objectMapper,
        @Value("${toss.payments.base-url:https://api.tosspayments.com}") String baseUrl,
        @Value("${toss.payments.secret-key:}") String secretKey
    ) {
        this.restClient = restClientBuilder.baseUrl(baseUrl).build();
        this.objectMapper = objectMapper;
        this.secretKey = secretKey;
    }

    @Override
    public PaymentConfirmResponse confirm(PaymentConfirmRequest request) {
        if (!StringUtils.hasText(secretKey)) {
            throw new RoomescapeException(ErrorCode.PAYMENT_SECRET_KEY_NOT_CONFIGURED);
        }

        try {
            return restClient.post()
                .uri("/v1/payments/confirm")
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader())
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
}
