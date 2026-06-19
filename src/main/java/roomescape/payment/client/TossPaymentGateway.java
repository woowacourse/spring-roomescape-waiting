package roomescape.payment.client;

import java.net.SocketTimeoutException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.exception.PaymentConnectionFailedException;
import roomescape.payment.PaymentGateway;
import roomescape.payment.exception.PaymentGatewayException;
import roomescape.payment.PaymentResult;
import roomescape.payment.PaymentStatus;
import roomescape.payment.exception.PaymentTimedOutException;
import roomescape.payment.client.dto.ConfirmRequest;
import roomescape.payment.client.dto.TossPaymentResponse;

/**
 * PaymentGateway 포트의 Toss 구현(어댑터). Toss 의 요청·응답·에러 포맷은 이 클래스 밖으로 새어 나가지 않는다(부패 방지 계층).
 */
@Component
public class TossPaymentGateway implements PaymentGateway {

    private final RestClient tossRestClient;

    public TossPaymentGateway(RestClient tossRestClient) {
        this.tossRestClient = tossRestClient;
    }

    @Override
    public PaymentResult confirm(PaymentConfirmation confirmation) {
        var request = new ConfirmRequest(
                confirmation.paymentKey(), confirmation.orderId(), confirmation.amount());
        try {
            TossPaymentResponse response = tossRestClient.post()
                    .uri("/v1/payments/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Idempotency-Key", confirmation.orderId())
                    .body(request)
                    .retrieve()
                    .body(TossPaymentResponse.class);
            return toResult(response);
        } catch (RestClientResponseException e) {
            throw new PaymentGatewayException(e);
        } catch (ResourceAccessException e) {
            if (isReadTimeout(e)) {
                throw new PaymentTimedOutException(e);
            }
            throw new PaymentConnectionFailedException(e);
        } catch (RestClientException e) {
            throw new PaymentTimedOutException(e);
        }
    }

    private boolean isReadTimeout(ResourceAccessException e) {
        return e.getCause() instanceof SocketTimeoutException ste
                && ste.getMessage() != null
                && ste.getMessage().toLowerCase().contains("read");
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
