package roomescape.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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
                .onStatus(status -> status.isError(), (req, resp) -> {
                    String message = "결제 승인에 실패했습니다.";
                    try {
                        byte[] body = resp.getBody().readAllBytes();
                        JsonNode node = objectMapper.readTree(body);
                        message = node.path("message").asText(message);
                    } catch (IOException ignored) {}
                    throw new TossPaymentException(message);
                })
                .body(TossPaymentResponse.class);
    }
}
