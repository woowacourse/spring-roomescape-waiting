package roomescape.payment.toss;

import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import roomescape.domain.PaymentConfirmation;
import roomescape.domain.PaymentGateway;
import roomescape.domain.PaymentResult;
import roomescape.domain.PaymentStatus;
import roomescape.exception.PaymentUncertainException;
import roomescape.payment.toss.dto.ConfirmRequest;
import roomescape.payment.toss.dto.TossPaymentResponse;

import java.io.IOException;

@Component
public class TossPaymentGateway implements PaymentGateway {

    private static final String CONFIRM_PATH = "/v1/payments/confirm";
    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    private static final int MAX_ATTEMPTS = 2;

    private final RestClient tossRestClient;

    public TossPaymentGateway(RestClient tossRestClient) {
        this.tossRestClient = tossRestClient;
    }

    @Override
    public PaymentResult confirm(PaymentConfirmation confirmation) {
        ConfirmRequest request = new ConfirmRequest(
                confirmation.paymentKey(),
                confirmation.orderId(),
                confirmation.amount()
        );

        for (int attempt = 1; ; attempt++) {
            try {
                return requestConfirm(request, confirmation.orderId());
            } catch (RestClientException e) {
                if (!isRetryable(e)) {
                    throw e;
                }
                if (attempt == MAX_ATTEMPTS) {
                    throw new PaymentUncertainException(
                            "결제 승인 결과를 확인하지 못했습니다. orderId=" + confirmation.orderId(), e);
                }
            }
        }
    }

    private PaymentResult requestConfirm(ConfirmRequest request, String idempotencyKey) {
        TossPaymentResponse response = tossRestClient.post()
                .uri(CONFIRM_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .header(IDEMPOTENCY_KEY_HEADER, idempotencyKey)
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw new TossPaymentException("토스 결제 승인에 실패했습니다. status=" + res.getStatusCode());
                })
                .body(TossPaymentResponse.class);

        PaymentStatus status = PaymentStatus.from(response.status());
        if (status != PaymentStatus.DONE) {
            throw new TossPaymentException("토스 결제가 완료되지 않았습니다. status=" + status);
        }
        return new PaymentResult(response.paymentKey(), response.orderId(), status);
    }

    private boolean isRetryable(RestClientException exception) {
        return NestedExceptionUtils.getRootCause(exception) instanceof IOException;
    }
}
