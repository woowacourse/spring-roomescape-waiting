package roomescape.payment.infrastructure;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import roomescape.common.exception.RoomEscapeException;
import roomescape.payment.application.PaymentGateway;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentResult;
import roomescape.payment.exception.PaymentErrorCode;
import roomescape.payment.infrastructure.dto.TossConfirmRequest;
import roomescape.payment.infrastructure.dto.TossConfirmResponse;
import roomescape.payment.infrastructure.dto.TossErrorResponse;

/**
 * Toss Payments 결제 승인 어댑터(부패 방지 계층, ACL).
 * Toss 전용 DTO와 에러 코드를 도메인 모델/도메인 예외로 번역해, Toss 의존이 밖으로 새지 않게 격리한다.
 */
@Component
public class TossPaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(TossPaymentGateway.class);
    private static final String CONFIRM_PATH = "/v1/payments/confirm";

    private static final Map<String, PaymentErrorCode> ERROR_CODES = Map.ofEntries(
            Map.entry("ALREADY_PROCESSED_PAYMENT", PaymentErrorCode.PAYMENT_ALREADY_PROCESSED),
            Map.entry("INVALID_REQUEST", PaymentErrorCode.PAYMENT_INVALID_REQUEST),
            Map.entry("DUPLICATED_ORDER_ID", PaymentErrorCode.PAYMENT_DUPLICATED_ORDER_ID),
            Map.entry("NOT_FOUND_PAYMENT_SESSION", PaymentErrorCode.PAYMENT_SESSION_EXPIRED),
            Map.entry("UNAUTHORIZED_KEY", PaymentErrorCode.PAYMENT_UNAUTHORIZED_KEY),
            Map.entry("INVALID_API_KEY", PaymentErrorCode.PAYMENT_UNAUTHORIZED_KEY),
            Map.entry("REJECT_CARD_PAYMENT", PaymentErrorCode.PAYMENT_REJECT_CARD),
            Map.entry("NOT_FOUND_PAYMENT", PaymentErrorCode.PAYMENT_NOT_FOUND),
            Map.entry("FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING", PaymentErrorCode.PAYMENT_PROVIDER_ERROR),
            Map.entry("FAILED_INTERNAL_SYSTEM_PROCESSING", PaymentErrorCode.PAYMENT_PROVIDER_ERROR)
    );

    private final RestClient tossRestClient;
    private final ObjectMapper objectMapper;

    public TossPaymentGateway(RestClient tossRestClient, ObjectMapper objectMapper) {
        this.tossRestClient = tossRestClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public PaymentResult confirm(PaymentConfirmation confirmation) {
        TossConfirmResponse response = tossRestClient.post()
                .uri(CONFIRM_PATH)
                .body(TossConfirmRequest.from(confirmation))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, clientResponse) -> {
                    throw translate(clientResponse);
                })
                .body(TossConfirmResponse.class);

        return response.toResult();
    }

    private RoomEscapeException translate(ClientHttpResponse clientResponse) throws IOException {
        TossErrorResponse error = objectMapper.readValue(clientResponse.getBody(), TossErrorResponse.class);
        PaymentErrorCode errorCode = ERROR_CODES.getOrDefault(error.code(), PaymentErrorCode.PAYMENT_FAILED);
        logByErrorCode(errorCode, error);
        return new RoomEscapeException(errorCode);
    }

    private void logByErrorCode(PaymentErrorCode errorCode, TossErrorResponse error) {
        if (errorCode == PaymentErrorCode.PAYMENT_UNAUTHORIZED_KEY) {
            log.error("[운영 알람] 결제 키 설정 오류 - code={}, message={}", error.code(), error.message());
            return;
        }
        if (errorCode == PaymentErrorCode.PAYMENT_PROVIDER_ERROR) {
            log.warn("[재시도 대상] 결제사 내부 오류 - code={}, message={}", error.code(), error.message());
            return;
        }
        log.info("결제 승인 실패 - code={}, message={}", error.code(), error.message());
    }
}
