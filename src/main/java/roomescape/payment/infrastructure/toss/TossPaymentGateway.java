package roomescape.payment.infrastructure.toss;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentGateway;
import roomescape.payment.domain.PaymentResult;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@RequiredArgsConstructor
public class TossPaymentGateway implements PaymentGateway {

    private static final String TOSS_CONFIRM_URI = "/v1/payments/confirm";

    private final RestClient tossRestClient;
    private final TossPaymentProperties properties;
    private final ObjectMapper objectMapper;

    @Override
    public PaymentResult confirm(final PaymentConfirmation confirmation) {
        validateSecretKey();

        try {
            final TossPaymentConfirmResponse response = tossRestClient.post()
                    .uri(TOSS_CONFIRM_URI)
                    .header(HttpHeaders.AUTHORIZATION, authorizationHeader())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new TossPaymentConfirmRequest(
                            confirmation.paymentKey(),
                            confirmation.orderId(),
                            confirmation.amount()
                    ))
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (request, clientResponse) -> {
                        final TossErrorResponse errorResponse = objectMapper.readValue(
                                clientResponse.getBody(),
                                TossErrorResponse.class
                        );
                        throw TossPaymentErrorMapper.map(clientResponse.getStatusCode(), errorResponse);
                    })
                    .body(TossPaymentConfirmResponse.class);

            return toPaymentResult(confirmation, response);
        } catch (RestClientException exception) {
            throw new IllegalStateException("결제 승인 서버와 통신하지 못했습니다.", exception);
        }
    }

    private void validateSecretKey() {
        if (properties.secretKey() == null || properties.secretKey().isBlank()) {
            throw new IllegalStateException("결제 시크릿 키가 설정되어 있지 않습니다.");
        }
    }

    private String authorizationHeader() {
        final byte[] credential = "%s:".formatted(properties.secretKey()).getBytes(StandardCharsets.UTF_8);

        return "Basic " + Base64.getEncoder().encodeToString(credential);
    }

    private PaymentResult toPaymentResult(
            final PaymentConfirmation confirmation,
            final TossPaymentConfirmResponse response
    ) {
        if (response == null) {
            throw new IllegalStateException("결제 승인 서버와 통신하지 못했습니다.");
        }

        final int approvedAmount = response.totalAmount() == null
                ? confirmation.amount()
                : response.totalAmount();

        return new PaymentResult(
                response.paymentKey(),
                response.orderId(),
                approvedAmount
        );
    }
}
