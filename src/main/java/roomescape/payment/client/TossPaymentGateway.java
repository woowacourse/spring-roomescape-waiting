package roomescape.payment.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import roomescape.payment.client.dto.CancelRequest;
import roomescape.payment.client.dto.ConfirmRequest;
import roomescape.payment.NetworkUncertain;
import roomescape.payment.client.dto.TossErrorResponse;
import roomescape.payment.client.dto.TossPaymentResponse;
import roomescape.payment.dto.PaymentResult;

@Component
public class TossPaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(TossPaymentGateway.class);

    private final RestClient tossRestClient;
    private final ObjectMapper objectMapper;

    public TossPaymentGateway(RestClient tossRestClient, ObjectMapper objectMapper) {
        this.tossRestClient = tossRestClient;
        this.objectMapper = objectMapper;
    }

    @Retryable(retryFor = TossPaymentException.Retryable.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public PaymentResult confirm(String paymentKey, String orderId, long amount) {
        log.info("결제 승인 요청 paymentKey={} orderId={} amount={}", paymentKey, orderId, amount);
        try {
            TossPaymentResponse response = tossRestClient.post()
                    .uri("/v1/payments/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Idempotency-Key", orderId)
                    .body(new ConfirmRequest(paymentKey, orderId, amount))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        var error = objectMapper.readValue(res.getBody(), TossErrorResponse.class);
                        throw TossPaymentException.of(res.getStatusCode(), error);
                    })
                    .body(TossPaymentResponse.class);
            log.info("결제 승인 완료 paymentKey={} orderId={}", paymentKey, orderId);
            return new PaymentResult(response.paymentKey(), response.orderId(), response.status(), response.totalAmount());
        } catch (ResourceAccessException e) {
            throw new NetworkUncertain();
        }
    }

    public void cancel(String paymentKey, String cancelReason) {
        log.info("결제 취소 요청 paymentKey={} reason={}", paymentKey, cancelReason);
        try {
            tossRestClient.post()
                    .uri("/v1/payments/{paymentKey}/cancel", paymentKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new CancelRequest(cancelReason))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        var error = objectMapper.readValue(res.getBody(), TossErrorResponse.class);
                        throw TossPaymentException.of(res.getStatusCode(), error);
                    })
                    .toBodilessEntity();
            log.info("결제 취소 완료 paymentKey={}", paymentKey);
        } catch (ResourceAccessException e) {
            throw new NetworkUncertain();
        }
    }
}