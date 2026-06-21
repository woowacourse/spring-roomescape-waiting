package roomescape.adapter.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import roomescape.adapter.payment.dto.TossConfirmRequest;
import roomescape.adapter.payment.dto.TossErrorResponse;
import roomescape.adapter.payment.dto.TossPaymentResponse;
import roomescape.domain.payment.PaymentConfirmation;
import roomescape.domain.payment.PaymentGateway;
import roomescape.domain.payment.PaymentResult;
import roomescape.domain.payment.PaymentStatus;
import roomescape.exception.client.PaymentAlreadyProcessedException;
import roomescape.exception.client.PaymentRejectedException;
import roomescape.exception.server.PaymentGatewayException;

/**
 * PaymentGateway 포트의 Toss 구현(어댑터). Toss의 요청·응답·에러 포맷과 code 문자열은 이 클래스 밖으로 새지 않는다(부패 방지 계층, ACL).
 */
@Component
public class TossPaymentGateway implements PaymentGateway {
    private static final Logger log = LoggerFactory.getLogger(TossPaymentGateway.class);

    private final RestClient tossRestClient;
    private final ObjectMapper objectMapper;

    public TossPaymentGateway(RestClient tossRestClient, ObjectMapper objectMapper) {
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
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    throw translate(readError(res));
                })
                .body(TossPaymentResponse.class);

        return toResult(response);
    }

    private TossErrorResponse readError(ClientHttpResponse response) {
        try {
            TossErrorResponse error = objectMapper.readValue(response.getBody(), TossErrorResponse.class);
            if (error == null || error.code() == null) {
                return new TossErrorResponse("UNKNOWN", "결제 오류 응답을 해석할 수 없습니다.");
            }
            return error;
        } catch (IOException e) {
            return new TossErrorResponse("UNKNOWN", "결제 오류 응답을 해석할 수 없습니다.");
        }
    }

    /**
     * Toss 에러 code를 도메인(RoomEscape) 예외로 번역한다. 정의되지 않은 코드는 게이트웨이 예외(500)로 떨어진다. code 문자열은 이 메서드 안에만 머문다.
     */
    private RuntimeException translate(TossErrorResponse error) {
        return switch (error.code()) {
            case "ALREADY_PROCESSED_PAYMENT" -> new PaymentAlreadyProcessedException("이미 처리된 결제입니다.");
            case "REJECT_CARD_PAYMENT" -> new PaymentRejectedException(error.message());
            case "DUPLICATED_ORDER_ID", "NOT_FOUND_PAYMENT_SESSION", "INVALID_REQUEST", "NOT_FOUND_PAYMENT" ->
                    new PaymentRejectedException("결제 요청이 올바르지 않거나 만료되었습니다. 다시 시도해 주세요.");
            case "UNAUTHORIZED_KEY", "INVALID_API_KEY", "FAILED_PAYMENT_INTERNAL_SYSTEM_PROCESSING" ->
                    new PaymentGatewayException("결제 게이트웨이 오류 [" + error.code() + "]: " + error.message());
            default -> {
                log.warn("정의되지 않은 토스 결제 에러 - code={}, message={}", error.code(), error.message());
                yield new PaymentGatewayException("정의되지 않은 결제 오류 [" + error.code() + "]: " + error.message());
            }
        };
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
