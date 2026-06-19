package roomescape.infrastructure.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import roomescape.domain.payment.PaymentConfirmation;
import roomescape.domain.payment.PaymentResult;
import roomescape.service.payment.port.PaymentGateway;

@Component
public class TossPaymentGateway implements PaymentGateway {

    private static final String CONFIRM_URI = "/v1/payments/confirm";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final String secretKey;

    public TossPaymentGateway(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            @Value("${payment.toss.base-url:https://api.tosspayments.com}") String baseUrl,
            @Value("${payment.toss.secret-key}") String secretKey
    ) {
        this.restClient = restClientBuilder
                .baseUrl(baseUrl)
                .build();
        this.objectMapper = objectMapper;
        this.secretKey = secretKey;
    }

    @Override
    public PaymentResult confirm(PaymentConfirmation confirmation) {
        TossConfirmResponse response = restClient.post()
                .uri(CONFIRM_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", authorization())
                .body(new TossConfirmRequest(confirmation.paymentKey(), confirmation.orderId(), confirmation.amount()))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, clientResponse) -> {
                    TossErrorResponse errorResponse = readErrorResponse(clientResponse.getBody().readAllBytes());
                    throw TossPaymentException.of(clientResponse.getStatusCode(), errorResponse);
                })
                .body(TossConfirmResponse.class);

        return new PaymentResult(response.paymentKey(), response.orderId(), response.totalAmount());
    }

    private String authorization() {
        String credential = secretKey + ":";
        String encoded = Base64.getEncoder().encodeToString(credential.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }

    private TossErrorResponse readErrorResponse(byte[] body) throws IOException {
        return objectMapper.readValue(body, TossErrorResponse.class);
    }
}
