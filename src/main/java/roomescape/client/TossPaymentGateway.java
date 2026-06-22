package roomescape.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import roomescape.client.dto.ConfirmRequest;
import roomescape.client.dto.TossErrorResponse;
import roomescape.client.dto.TossPaymentResponse;
import roomescape.domain.PaymentConfirmation;
import roomescape.domain.PaymentResult;
import roomescape.service.PaymentGateway;

import java.net.ConnectException;
import java.net.SocketTimeoutException;

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

        try {
            TossPaymentResponse response = tossRestClient.post()
                    .uri("/v1/payments/confirm")
                    .header("Idempotency-Key", confirmation.idempotencyKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (httpRequest, clientHttpResponse) -> {
                        TossErrorResponse error = objectMapper.readValue(clientHttpResponse.getBody(), TossErrorResponse.class);
                        throw TossPaymentException.of(clientHttpResponse.getStatusCode(), error);
                    })
                    .body(TossPaymentResponse.class);

            return new PaymentResult(response.paymentKey(), response.orderId(), response.totalAmount());
        } catch (TossPaymentException exception) {
            throw exception;
        } catch (ResourceAccessException exception) {
            throw mapResourceAccessException(exception);
        } catch (RestClientException exception) {
            throw mapRestClientException(exception);
        }
    }

    private PaymentGatewayException mapResourceAccessException(ResourceAccessException exception) {
        Throwable rootCause = rootCause(exception);

        if (rootCause instanceof SocketTimeoutException) {
            return new PaymentGatewayException.ReadTimeout(exception);
        }
        if (rootCause instanceof ConnectException) {
            return new PaymentGatewayException.ConnectionFailed(exception);
        }

        return new PaymentGatewayException.Unknown(exception);
    }

    private PaymentGatewayException mapRestClientException(RestClientException exception) {
        Throwable rootCause = rootCause(exception);

        if (rootCause instanceof SocketTimeoutException) {
            return new PaymentGatewayException.ReadTimeout(exception);
        }

        return new PaymentGatewayException.Unknown(exception);
    }

    private Throwable rootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        return current;
    }
}
