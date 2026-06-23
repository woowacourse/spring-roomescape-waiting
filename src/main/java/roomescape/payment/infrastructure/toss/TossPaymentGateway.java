package roomescape.payment.infrastructure.toss;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentGateway;
import roomescape.payment.domain.PaymentResult;
import roomescape.payment.domain.PaymentStatus;
import roomescape.payment.domain.exception.PaymentGatewayConfigurationException;
import roomescape.payment.domain.exception.PaymentGatewayException;
import roomescape.payment.domain.exception.RetryablePaymentException;
import roomescape.payment.infrastructure.toss.dto.TossConfirmRequest;
import roomescape.payment.infrastructure.toss.dto.TossErrorResponse;
import roomescape.payment.infrastructure.toss.dto.TossPaymentResponse;

@Component
public class TossPaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(TossPaymentGateway.class);

    private final RestClient tossRestClient;
    private final ObjectMapper objectMapper;

    public TossPaymentGateway(
            @Qualifier("tossRestClient") RestClient tossRestClient,
            ObjectMapper objectMapper
    ) {
        this.tossRestClient = tossRestClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public PaymentResult confirm(PaymentConfirmation confirmation) {
        TossConfirmRequest request = new TossConfirmRequest(
                confirmation.paymentKey(), confirmation.orderId(), confirmation.amount());
        TossPaymentResponse response = tossRestClient.post()
                .uri("/v1/payments/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (httpRequest, httpResponse) -> {
                    try {
                        TossErrorResponse error = objectMapper.readValue(
                                httpResponse.getBody(), TossErrorResponse.class);
                        RuntimeException exception = TossPaymentErrorMapper.map(error.code());
                        logExternalFailure(exception, error);
                        throw exception;
                    } catch (IOException e) {
                        log.error("Toss 결제 승인 에러 응답을 역직렬화하지 못했습니다.", e);
                        throw new PaymentGatewayException();
                    }
                })
                .body(TossPaymentResponse.class);

        if (response == null) {
            log.error("Toss 결제 승인 응답 본문이 비어 있습니다. orderId={}", confirmation.orderId());
            throw new PaymentGatewayException();
        }
        return new PaymentResult(
                response.paymentKey(),
                response.orderId(),
                PaymentStatus.from(response.status()),
                response.totalAmount()
        );
    }

    private void logExternalFailure(RuntimeException exception, TossErrorResponse error) {
        if (exception instanceof PaymentGatewayConfigurationException) {
            log.error("Toss 인증 실패 — API 키 설정을 확인해야 합니다. code={}, message={}",
                    error.code(), error.message());
        } else if (exception instanceof RetryablePaymentException) {
            log.warn("Toss 내부 오류 — 재시도할 수 있습니다. code={}, message={}",
                    error.code(), error.message());
        }
    }
}
