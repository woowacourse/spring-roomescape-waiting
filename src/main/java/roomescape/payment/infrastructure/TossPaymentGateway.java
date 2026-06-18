package roomescape.payment.infrastructure;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentResult;
import roomescape.payment.domain.PaymentStatus;
import roomescape.payment.infrastructure.dto.ConfirmRequest;
import roomescape.payment.infrastructure.dto.TossErrorResponse;
import roomescape.payment.infrastructure.dto.TossPaymentResponse;
import roomescape.payment.service.PaymentGateway;

@Component
public class TossPaymentGateway implements PaymentGateway {

    private final RestClient tossRestClient;
    private final ObjectMapper objectMapper;

    public TossPaymentGateway(RestClient tossRestClient, ObjectMapper objectMapper) {
        this.tossRestClient = tossRestClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public PaymentResult confirm(PaymentConfirmation confirmation) {
        ConfirmRequest request = new ConfirmRequest(
                confirmation.paymentKey(),
                confirmation.orderId(),
                confirmation.amount()
        );

        TossPaymentResponse response = tossRestClient.post()
                .uri("/v1/payments/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                // 타임아웃으로 끊긴 뒤 재시도해도 같은 결제로 식별되도록 멱등키를 싣는다(주문당 고정값).
                .header("Idempotency-Key", confirmation.idempotencyKey())
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, this::mapError)
                .body(TossPaymentResponse.class);

        return new PaymentResult(
                response.paymentKey(),
                response.orderId(),
                PaymentStatus.from(response.status()),
                response.totalAmount()
        );
    }

    private void mapError(HttpRequest request, ClientHttpResponse response) throws IOException {
        String body = StreamUtils.copyToString(response.getBody(), StandardCharsets.UTF_8);
        TossErrorResponse errorResponse = objectMapper.readValue(body, TossErrorResponse.class);
        throw TossPaymentException.of(response.getStatusCode(), errorResponse.code(), errorResponse.message());
    }
}
