package roomescape.adapter.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import roomescape.adapter.payment.dto.TossConfirmRequest;
import roomescape.adapter.payment.dto.TossErrorResponse;
import roomescape.adapter.payment.dto.TossPaymentResponse;
import roomescape.domain.payment.PaymentConfirmation;
import roomescape.domain.payment.PaymentGateway;
import roomescape.domain.payment.PaymentResult;
import roomescape.domain.payment.PaymentStatus;
import roomescape.exception.client.PaymentAlreadyProcessedException;
import roomescape.exception.client.PaymentRejectedException;
import roomescape.exception.server.PaymentConnectionException;
import roomescape.exception.server.PaymentGatewayException;
import roomescape.exception.server.PaymentTimeoutException;

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
        try {
            TossPaymentResponse response = tossRestClient.post()
                    .uri("/v1/payments/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Idempotency-Key", confirmation.orderId())// req3: 주문당 고정 키
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw translate(readError(res));// 토스 에러(거절 등)는 여기서 번역
                    })
                    .body(TossPaymentResponse.class);

            return toResult(response);
        } catch (RestClientException e) {                              // req2: 전송 실패(타임아웃/연결)
            throw translateTransport(e);
        }

    }

    /**
     * 전송 실패를 도메인 예외로 번역한다. 연결 거부/DNS 실패는 '도달 못 함(안전)', 그 외 타임아웃은 '도달했을 수도(확인 필요)'로 본다. 연결/읽기 타임아웃은 둘 다
     * SocketTimeoutException이라 구분이 모호한데, 애매하면 안전하게 '확인 필요'로 기운다.
     */
    private RuntimeException translateTransport(RestClientException e) {
        Throwable cause = NestedExceptionUtils.getMostSpecificCause(e);
        if (cause instanceof ConnectException || cause instanceof UnknownHostException) {
            return new PaymentConnectionException("결제 서버에 연결하지 못했습니다. 결제는 진행되지 않았으니 다시 시도해 주세요.");
        }
        return new PaymentTimeoutException("결제 결과를 확인하지 못했습니다. 잠시 후 결제 내역에서 상태를 확인해 주세요.");
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
