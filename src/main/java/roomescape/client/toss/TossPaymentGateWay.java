package roomescape.client.toss;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.ResourceAccessException;
import roomescape.client.PaymentConfirmationUnknownException;
import roomescape.client.PaymentConfirmation;
import roomescape.client.PaymentGateway;
import roomescape.client.PaymentResult;
import roomescape.client.PaymentStatus;
import roomescape.client.toss.dto.ConfirmRequest;
import roomescape.client.toss.dto.TossErrorResponse;
import roomescape.client.toss.dto.TossPaymentResponse;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

@Component
public class TossPaymentGateWay implements PaymentGateway {

    private final RestClient tossRestClient;
    private final ObjectMapper objectMapper;

    public TossPaymentGateWay(RestClient tossRestClient, ObjectMapper objectMapper) {
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
        TossPaymentResponse response;
        try {
            response = tossRestClient.post()
                    .uri("/v1/payments/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Idempotency-Key", confirmation.idempotencyKey())
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        TossErrorResponse error = objectMapper.readValue(res.getBody(), TossErrorResponse.class);
                        throw TossPaymentException.toDomainException(error);
                    })
                    .body(TossPaymentResponse.class);
        } catch (ResourceAccessException e) {
            if (hasCause(e, ConnectException.class) || hasCause(e, SocketTimeoutException.class)) {
                throw new PaymentConfirmationUnknownException("결제 승인 응답을 받지 못했습니다. 결제 내역 확인이 필요합니다.", e);
            }
            throw e;
        } catch (RestClientException e) {
            if (hasCause(e, SocketTimeoutException.class)) {
                throw new PaymentConfirmationUnknownException("결제 승인 응답을 읽지 못했습니다. 결제 내역 확인이 필요합니다.", e);
            }
            throw e;
        }

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
}
