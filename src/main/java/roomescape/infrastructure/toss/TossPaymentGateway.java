package roomescape.infrastructure.toss;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import roomescape.infrastructure.toss.dto.ConfirmRequest;
import roomescape.infrastructure.toss.dto.TossErrorResponse;
import roomescape.infrastructure.toss.dto.TossPaymentResponse;

@Component
public class TossPaymentGateway {
    private static final String CONFIRM_URI = "/v1/payments/confirm";

    private final RestClient tossRestClient;
    private final ObjectMapper objectMapper;

    @Value("${payment.toss.secret-key}")
    private String secretKey;

    public TossPaymentGateway(RestClient.Builder restClientBuilder, ObjectMapper objectMapper
    ) {
        this.tossRestClient = restClientBuilder
                .baseUrl("https://api.tosspayments.com")
                .build();
        this.objectMapper = objectMapper;
    }

    public void confirm(String paymentKey, String orderId, Long amount) {
        tossRestClient.post()
                .uri(CONFIRM_URI)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ConfirmRequest(paymentKey, orderId, amount))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    TossErrorResponse error = objectMapper.readValue(response.getBody(), TossErrorResponse.class);
                    throw TossPaymentException.of(response.getStatusCode(), error);
                })
                .body(TossPaymentResponse.class);
    }

    private String authorizationHeader() {
        String credentials = secretKey + ":";
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }
}
