package roomescape.payment.toss;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentConnectionException;
import roomescape.payment.PaymentGateway;
import roomescape.payment.PaymentResult;
import roomescape.payment.PaymentResultUnknownException;
import roomescape.payment.toss.dto.ConfirmRequest;
import roomescape.payment.toss.dto.TossErrorResponse;
import roomescape.payment.toss.dto.TossPaymentResponse;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Locale;

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
            TossPaymentResponse response = tossRestClient.post()
                    .uri("/v1/payments/confirm")
                    .header("Idempotency-Key", confirmation.orderId())
                    .body(new ConfirmRequest(confirmation.paymentKey(), confirmation.orderId(), confirmation.amount()))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, resp) -> {
                        TossErrorResponse error = objectMapper.readValue(resp.getBody(), TossErrorResponse.class);
                        throw TossPaymentException.of(resp.getStatusCode(), error);
                    })
                    .body(TossPaymentResponse.class);
            return response.toPaymentResult();
        } catch (ResourceAccessException e) {
            throw translateTransportError(e);
        }
    }

    private RuntimeException translateTransportError(ResourceAccessException e) {
        Throwable cause = e.getMostSpecificCause();

        if (cause instanceof ConnectException) {
            return new PaymentConnectionException("결제 서버에 연결하지 못했습니다.", e);
        }
        if (cause instanceof SocketTimeoutException) {
            String message = cause.getMessage() == null ? "" : cause.getMessage().toLowerCase(Locale.ROOT);
            if (message.contains("connect")) {
                return new PaymentConnectionException("결제 서버 연결이 시간 내에 이뤄지지 않았습니다.", e);
            }
            return new PaymentResultUnknownException("결제 결과를 확인하지 못했습니다.", e);
        }
        return new PaymentResultUnknownException("결제 결과를 확인하지 못했습니다.", e);
    }
}
