package roomescape.payment.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.SocketTimeoutException;
import java.util.Objects;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentGateway;
import roomescape.payment.PaymentResult;
import roomescape.payment.client.dto.ConfirmRequest;
import roomescape.payment.client.dto.TossErrorResponse;
import roomescape.payment.client.dto.TossPaymentResponse;

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
        try {
            TossPaymentResponse tossResponse = Objects.requireNonNull(
                    tossRestClient.post()
                            .uri("/v1/payments/confirm")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("Idempotency-Key", confirmation.orderId())
                            .body(new ConfirmRequest(confirmation.paymentKey(), confirmation.orderId(), confirmation.amount()))
                            .retrieve()
                            .onStatus(HttpStatusCode::isError, (request, response) -> {
                                TossErrorResponse error = objectMapper.readValue(response.getBody(), TossErrorResponse.class);
                                throw TossPaymentException.of(response.getStatusCode(), error);
                            })
                            .body(TossPaymentResponse.class)
            );
            return new PaymentResult(tossResponse.paymentKey(), tossResponse.orderId(), tossResponse.totalAmount());
        } catch (ResourceAccessException e) {
            if (isReadTimeout(e)) {
                throw new PaymentReadTimeoutException("결제 서버 응답 대기 시간을 초과했습니다. 결제 결과를 확인해 주세요.", e);
            }
            throw new PaymentNetworkException("결제 서버에 연결할 수 없습니다.", e);
        }
    }

    private boolean isReadTimeout(ResourceAccessException e) {
        Throwable cause = e.getCause();
        return cause instanceof SocketTimeoutException ste
                && ste.getMessage() != null
                && ste.getMessage().toLowerCase().contains("read");
    }
}