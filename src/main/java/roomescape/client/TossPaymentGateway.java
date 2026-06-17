package roomescape.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import roomescape.client.dto.TossErrorResponse;
import roomescape.client.dto.TossPaymentResponse;

@Component
public class TossPaymentGateway {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public TossPaymentGateway(
            @Value("${toss.base-url}") String baseUrl,
            @Value("${toss.secret-key}") String secretKey,
            ObjectMapper objectMapper) {
        String encoded = Base64.getEncoder()
                .encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Basic " + encoded)
                .build();
        this.objectMapper = objectMapper;
    }

    public TossPaymentResponse confirm(String paymentKey, String orderId, Long amount) {
        record ConfirmRequest(String paymentKey, String orderId, Long amount) {}
        return restClient.post()
                .uri("/v1/payments/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ConfirmRequest(paymentKey, orderId, amount))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, resp) -> {
                    var error = objectMapper.readValue(resp.getBody(), TossErrorResponse.class);
                    throw new TossPaymentException(error.message());
                })
                .body(TossPaymentResponse.class);
    }
}
