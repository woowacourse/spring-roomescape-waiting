package roomescape.payment.toss;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.SocketTimeoutException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentConnectionException;
import roomescape.payment.PaymentConfirmUnknownException;
import roomescape.payment.PaymentGateway;
import roomescape.payment.PaymentResult;
import roomescape.payment.toss.dto.TossConfirmRequest;
import roomescape.payment.toss.dto.TossErrorResponse;
import roomescape.payment.toss.dto.TossPaymentResponse;

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
            TossPaymentResponse response = requestConfirm(confirmation);
            return toResult(response);
        } catch (ResourceAccessException e) {
            throw handleResourceAccessException(e);
        } catch (RestClientException e) {
            throw handleRestClientException(e);
        }
    }

    private TossPaymentResponse requestConfirm(PaymentConfirmation confirmation) {
        TossConfirmRequest request = new TossConfirmRequest(
                confirmation.paymentKey(),
                confirmation.orderId(),
                confirmation.amount()
        );
        return tossRestClient.post()
                .uri("/v1/payments/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Idempotency-Key", confirmation.idempotencyKey())
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    TossErrorResponse error = objectMapper.readValue(res.getBody(), TossErrorResponse.class);
                    throw TossPaymentException.of(res.getStatusCode(), error);
                })
                .body(TossPaymentResponse.class);
    }

    private RuntimeException handleResourceAccessException(ResourceAccessException e) {
        if (hasCause(e, SocketTimeoutException.class) && hasMessage(e, "Read timed out")) {
            return new PaymentConfirmUnknownException("토스 결제 승인 응답을 받지 못했습니다. 결제 내역에서 결과를 확인해 주세요.", e);
        }
        return new PaymentConnectionException("토스 결제 서버에 연결하지 못했습니다. 잠시 후 다시 시도해 주세요.", e);
    }

    private RuntimeException handleRestClientException(RestClientException e) {
        if (hasCause(e, SocketTimeoutException.class)) {
            return new PaymentConfirmUnknownException("토스 결제 승인 응답을 받지 못했습니다. 결제 내역에서 결과를 확인해 주세요.", e);
        }
        return e;
    }

    private PaymentResult toResult(TossPaymentResponse response) {
        return new PaymentResult(
                response.paymentKey(),
                response.orderId(),
                response.status(),
                response.totalAmount()
        );
    }

    private boolean hasCause(Throwable throwable, Class<? extends Throwable> causeType) {
        Throwable current = throwable;
        while (current != null) {
            if (causeType.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private boolean hasMessage(Throwable throwable, String message) {
        Throwable current = throwable;
        while (current != null) {
            if (current.getMessage() != null && current.getMessage().contains(message)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }
}
