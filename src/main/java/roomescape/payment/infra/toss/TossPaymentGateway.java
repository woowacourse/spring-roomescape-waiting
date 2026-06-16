package roomescape.payment.infra.toss;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import roomescape.payment.config.PaymentProperties;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentGateway;
import roomescape.payment.domain.PaymentResult;
import roomescape.payment.domain.exception.PaymentGatewayException;
import roomescape.payment.domain.exception.PaymentKeyConfigurationException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class TossPaymentGateway implements PaymentGateway {
    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final PaymentProperties paymentProperties;

    public TossPaymentGateway(
            RestClient.Builder restClientBuilder,
            ObjectMapper objectMapper,
            PaymentProperties paymentProperties
    ) {
        this.restClient = restClientBuilder
                .baseUrl(paymentProperties.toss().baseUrl())
                .build();
        this.objectMapper = objectMapper;
        this.paymentProperties = paymentProperties;
    }

    @Override
    public PaymentResult confirm(PaymentConfirmation confirmation) {
        TossPaymentConfirmResponse response = restClient.post()
                .uri("/v1/payments/confirm")
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader())
                .contentType(MediaType.APPLICATION_JSON)
                .body(new TossPaymentConfirmRequest(
                        confirmation.paymentKey(),
                        confirmation.orderId(),
                        confirmation.amount()
                ))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, clientResponse) -> {
                    TossErrorResponse errorResponse = readErrorResponse(clientResponse.getBody());
                    throw TossPaymentExceptionMapper.map(clientResponse.getStatusCode(), errorResponse);
                })
                .body(TossPaymentConfirmResponse.class);

        if (response == null) {
            throw new PaymentGatewayException("결제 승인 응답이 비어 있습니다.");
        }

        return new PaymentResult(response.paymentKey(), response.orderId(), response.totalAmount());
    }

    private TossErrorResponse readErrorResponse(java.io.InputStream body) {
        try {
            return objectMapper.readValue(body, TossErrorResponse.class);
        } catch (IOException exception) {
            throw new PaymentGatewayException("결제 승인 실패 응답을 해석하지 못했습니다.");
        }
    }

    private String authorizationHeader() {
        String secretKey = paymentProperties.toss().secretKey();
        if (secretKey == null || secretKey.isBlank()) {
            throw new PaymentKeyConfigurationException("Toss Payments 시크릿 키가 설정되지 않았습니다.");
        }

        byte[] token = (secretKey + ":").getBytes(StandardCharsets.UTF_8);
        return "Basic " + Base64.getEncoder().encodeToString(token);
    }
}
