package roomescape.infrastructure.payment.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import roomescape.domain.payment.PaymentConfirmation;
import roomescape.domain.payment.PaymentGateway;
import roomescape.domain.payment.PaymentResult;
import roomescape.exception.code.PaymentErrorCode;
import roomescape.exception.domain.PaymentException;
import roomescape.infrastructure.payment.client.dto.TossConfirmRequest;
import roomescape.infrastructure.payment.client.dto.TossConfirmResponse;
import roomescape.infrastructure.payment.client.dto.TossErrorResponse;

@Component
public class TossPaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(TossPaymentGateway.class);
    private static final String CONFIRM_PATH = "/v1/payments/confirm";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public TossPaymentGateway(RestClient tossRestClient, ObjectMapper objectMapper) {
        this.restClient = tossRestClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public PaymentResult confirm(PaymentConfirmation confirmation) {
        TossConfirmRequest request = new TossConfirmRequest(
                confirmation.paymentKey(),
                confirmation.orderId(),
                confirmation.amount()
        );

        TossConfirmResponse response = restClient.post()
                .uri(CONFIRM_PATH)
                .body(request)
                .retrieve()
                .onStatus(status -> status.isError(), (req, res) -> {
                    TossErrorResponse error = parseErrorBody(res.getBody().readAllBytes());
                    log.warn("Toss 결제 승인 실패: httpStatus={}, code={}, message={}",
                            res.getStatusCode(), error.code(), error.message());
                    throw toPaymentException(error);
                })
                .body(TossConfirmResponse.class);

        log.info("Toss 결제 승인 성공: orderId={}, paymentKey={}", confirmation.orderId(), response.paymentKey());
        return new PaymentResult(response.paymentKey());
    }

    private TossErrorResponse parseErrorBody(byte[] body) {
        try {
            return objectMapper.readValue(body, TossErrorResponse.class);
        } catch (IOException e) {
            log.warn("Toss 에러 응답 파싱 실패", e);
            return new TossErrorResponse("UNKNOWN", "결제 오류 응답을 파싱할 수 없습니다.");
        }
    }

    private PaymentException toPaymentException(TossErrorResponse error) {
        PaymentErrorCode errorCode = switch (error.code()) {
            case "ALREADY_PROCESSED_PAYMENT" -> PaymentErrorCode.ALREADY_PROCESSED_PAYMENT;
            case "DUPLICATED_ORDER_ID" -> PaymentErrorCode.DUPLICATED_ORDER_ID;
            case "NOT_FOUND_PAYMENT_SESSION" -> PaymentErrorCode.NOT_FOUND_PAYMENT_SESSION;
            case "INVALID_REQUEST" -> PaymentErrorCode.INVALID_REQUEST;
            case "UNAUTHORIZED_KEY" -> PaymentErrorCode.UNAUTHORIZED_KEY;
            case "INVALID_API_KEY" -> PaymentErrorCode.INVALID_API_KEY;
            case "REJECT_CARD_PAYMENT" -> PaymentErrorCode.REJECT_CARD_PAYMENT;
            case "NOT_FOUND_PAYMENT" -> PaymentErrorCode.NOT_FOUND_PAYMENT;
            case "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING" ->
                    PaymentErrorCode.FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING;
            default -> {
                log.warn("정의되지 않은 Toss 에러 코드: code={}, message={}", error.code(), error.message());
                yield PaymentErrorCode.PAYMENT_GATEWAY_ERROR;
            }
        };
        return new PaymentException(errorCode);
    }
}
