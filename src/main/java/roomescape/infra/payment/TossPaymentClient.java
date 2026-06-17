package roomescape.infra.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import roomescape.exception.client.BusinessRuleViolationException;

@Component
public class TossPaymentClient {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public TossPaymentClient(
            @Value("${toss.base-url}") String baseUrl,
            @Value("${toss.secret-key:}") String secretKey,
            ObjectMapper objectMapper
    ) {
        String basic = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + basic)
                .build();
        this.objectMapper = objectMapper;
    }

    public TossPaymentResponse confirm(String paymentKey, String orderId, Long amount) {
        return restClient.post()
                .uri("/v1/payments/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new TossConfirmRequest(paymentKey, orderId, amount))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    TossErrorResponse error = objectMapper.readValue(response.getBody(), TossErrorResponse.class);
                    throw new BusinessRuleViolationException(error.message());
                })
                .body(TossPaymentResponse.class);
    }
}
