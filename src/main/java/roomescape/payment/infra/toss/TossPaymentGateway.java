package roomescape.payment.infra.toss;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import roomescape.payment.application.exception.PaymentErrorCode;
import roomescape.payment.application.exception.PaymentException;
import roomescape.payment.config.PaymentProperties;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentGateway;
import roomescape.payment.domain.PaymentResult;

@RequiredArgsConstructor
@Component
public class TossPaymentGateway implements PaymentGateway {

    private final RestClient tossRestClient;
    private final ObjectMapper objectMapper;
    private final PaymentProperties properties;

    @Override
    public PaymentResult confirm(PaymentConfirmation confirmation) {
        try {
            TossConfirmResponse response = tossRestClient.post()
                    .uri("/v1/payments/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(HttpHeaders.AUTHORIZATION, authorization())
                    .body(TossConfirmRequest.from(confirmation))
                    .retrieve()
                    .onStatus(status -> status.isError(), (request, clientResponse) -> {
                        throw mapError(clientResponse);
                    })
                    .body(TossConfirmResponse.class);

            if (response == null) {
                throw new PaymentException(PaymentErrorCode.UNKNOWN_GATEWAY_ERROR);
            }
            return response.toResult();
        } catch (PaymentException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new PaymentException(PaymentErrorCode.UNKNOWN_GATEWAY_ERROR);
        }
    }

    private String authorization() {
        String credential = properties.toss().secretKey() + ":";
        String encoded = Base64.getEncoder().encodeToString(credential.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }

    private PaymentException mapError(ClientHttpResponse response) {
        try {
            TossErrorResponse error = objectMapper.readValue(response.getBody(), TossErrorResponse.class);
            return TossPaymentErrorMapper.map(error);
        } catch (IOException exception) {
            return new PaymentException(PaymentErrorCode.UNKNOWN_GATEWAY_ERROR);
        }
    }
}
