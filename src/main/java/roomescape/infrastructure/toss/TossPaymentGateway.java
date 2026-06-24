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

import roomescape.domain.PaymentConfirmation;
import roomescape.domain.PaymentGateway;
import roomescape.domain.PaymentResult;
import roomescape.domain.PaymentStatus;
import roomescape.infrastructure.toss.dto.ConfirmRequest;
import roomescape.infrastructure.toss.dto.TossErrorResponse;
import roomescape.infrastructure.toss.dto.TossPaymentResponse;

@Component
public class TossPaymentGateway implements PaymentGateway {
    private static final String CONFIRM_URI = "/v1/payments/confirm";

    private final RestClient tossRestClient;
    private final ObjectMapper objectMapper;

    @Value("${payment.toss.secret-key}")
    private String secretKey;

    public TossPaymentGateway(RestClient tossRestClient, ObjectMapper objectMapper
    ) {
        this.tossRestClient = tossRestClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public PaymentResult confirm(PaymentConfirmation confirmation) {
        TossPaymentResponse response = tossRestClient.post()
                .uri(CONFIRM_URI)
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ConfirmRequest(confirmation.paymentKey(), confirmation.orderId(), confirmation.amount()))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    TossErrorResponse error = objectMapper.readValue(response.getBody(), TossErrorResponse.class);
                    throw TossPaymentException.of(response.getStatusCode(), error);
                })
                .body(TossPaymentResponse.class);
        return toResult(response);
    }

    private PaymentResult toResult(TossPaymentResponse response) {
        return new PaymentResult(
                response.paymentKey(),
                response.orderId(),
                PaymentStatus.from(response.status()),
                response.totalAmount()
        );
    }

    private String authorizationHeader() {
        String credentials = secretKey + ":";
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }
}
