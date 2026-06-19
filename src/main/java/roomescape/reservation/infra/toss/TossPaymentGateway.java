package roomescape.reservation.infra.toss;

import java.io.IOException;
import java.net.SocketTimeoutException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import roomescape.global.exception.PaymentAlreadyProcessedException;
import roomescape.global.exception.PaymentCardRejectedException;
import roomescape.global.exception.PaymentGatewayConfigurationException;
import roomescape.global.exception.PaymentGatewayException;
import roomescape.global.exception.PaymentInvalidRequestException;
import roomescape.global.exception.PaymentNotFoundException;
import roomescape.global.exception.RetryablePaymentGatewayException;
import roomescape.reservation.application.port.out.payment.PaymentConfirmation;
import roomescape.reservation.application.port.out.payment.PaymentGateway;
import roomescape.reservation.application.port.out.payment.PaymentResult;
import roomescape.reservation.infra.toss.dto.ConfirmRequest;
import roomescape.reservation.infra.toss.dto.TossErrorResponse;
import roomescape.reservation.infra.toss.dto.TossPaymentResponse;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
@Component
public class TossPaymentGateway implements PaymentGateway {

    private static final int MAX_CONFIRM_ATTEMPTS = 3;

    private final RestClient tossRestClient;
    private final ObjectMapper objectMapper;

    @Override
    public PaymentResult confirm(PaymentConfirmation confirmation) {
        ConfirmRequest request = new ConfirmRequest(
                confirmation.paymentKey(),
                confirmation.orderId(),
                confirmation.amount()
        );

        for (int attempt = 1; attempt <= MAX_CONFIRM_ATTEMPTS; attempt++) {
            try {
                return requestConfirm(confirmation, request);
            } catch (RetryablePaymentGatewayException e) {
                if (attempt == MAX_CONFIRM_ATTEMPTS) {
                    throw e;
                }
            }
        }

        throw new PaymentGatewayException("결제 승인에 실패했습니다.");
    }

    private PaymentResult requestConfirm(PaymentConfirmation confirmation, ConfirmRequest request) {
        TossPaymentResponse response;
        try {
            response = tossRestClient.post()
                    .uri("/v1/payments/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Idempotency-Key", confirmation.orderId())
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (httpRequest, httpResponse) -> {
                        throw convertTossErrorToException(httpResponse);
                    })
                    .body(TossPaymentResponse.class);
        } catch (ResourceAccessException e) {
            throw new RetryablePaymentGatewayException(e);
        } catch (RestClientException e) {
            if (hasCause(e, SocketTimeoutException.class)) {
                throw new RetryablePaymentGatewayException(e);
            }
            throw e;
        }

        return response.toResult();
    }

    private boolean hasCause(Throwable exception, Class<? extends Throwable> causeType) {
        Throwable current = exception;
        while (current != null) {
            if (causeType.isInstance(current)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private RuntimeException convertTossErrorToException(ClientHttpResponse response) {
        TossErrorResponse error;
        try {
            error = objectMapper.readValue(response.getBody(), TossErrorResponse.class);
        } catch (IOException | JacksonException e) {
            return new PaymentGatewayException("결제 승인에 실패했습니다.");
        }

        if (error == null) {
            return new PaymentGatewayException("결제 승인에 실패했습니다.");
        }

        return switch (error.code()) {
            case "ALREADY_PROCESSED_PAYMENT" -> new PaymentAlreadyProcessedException();
            case "DUPLICATED_ORDER_ID", "NOT_FOUND_PAYMENT_SESSION", "INVALID_REQUEST" ->
                    new PaymentInvalidRequestException();
            case "UNAUTHORIZED_KEY", "INVALID_API_KEY" -> new PaymentGatewayConfigurationException();
            case "REJECT_CARD_PAYMENT" -> new PaymentCardRejectedException();
            case "NOT_FOUND_PAYMENT" -> new PaymentNotFoundException();
            case "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING" -> new RetryablePaymentGatewayException();
            case null, default -> new PaymentGatewayException("결제 승인에 실패했습니다.");
        };
    }
}
