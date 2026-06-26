package roomescape.payment.client;

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
import roomescape.payment.PaymentGateway;
import roomescape.payment.PaymentResult;
import roomescape.payment.PaymentStatus;
import roomescape.payment.PaymentTimeoutException;
import roomescape.payment.client.dto.CancelRequest;
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
        ConfirmRequest request = new ConfirmRequest(
                confirmation.paymentKey(), confirmation.orderId(), confirmation.amount());
        try {
            TossPaymentResponse response = tossRestClient.post()
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
            return toResult(response);
        } catch (ResourceAccessException e) {
            throw new PaymentConnectionException("결제 서버에 연결하지 못했습니다.", e);
        } catch (RestClientException e) {
            if (isReadTimeout(e)) {
                throw new PaymentTimeoutException("결제 승인 응답을 받지 못해 결과가 불확실합니다.", e);
            }
            throw e;
        }
    }

    private boolean isReadTimeout(Throwable e) {
        Throwable cause = e;
        while (cause != null) {
            if (cause instanceof SocketTimeoutException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    @Override
    public void cancel(String paymentKey, String reason) {
        tossRestClient.post()
                .uri("/v1/payments/{paymentKey}/cancel", paymentKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CancelRequest(reason))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    TossErrorResponse error = objectMapper.readValue(res.getBody(), TossErrorResponse.class);
                    throw TossPaymentException.of(res.getStatusCode(), error);
                })
                .toBodilessEntity();
    }

    private PaymentResult toResult(TossPaymentResponse response) {
        return new PaymentResult(
                response.paymentKey(),
                response.orderId(),
                PaymentStatus.from(response.status()),
                response.totalAmount()
        );
    }
}
