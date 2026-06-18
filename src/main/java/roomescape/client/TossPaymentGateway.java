package roomescape.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import roomescape.client.dto.ConfirmRequest;
import roomescape.client.dto.TossErrorResponse;
import roomescape.client.dto.TossPaymentResponse;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentConnectionFailedException;
import roomescape.payment.PaymentGateway;
import roomescape.payment.PaymentResult;
import roomescape.payment.PaymentResultUnknownException;
import roomescape.payment.PaymentStatus;

/**
 * PaymentGateway 포트의 Toss 구현(어댑터). Toss 의 요청·응답·에러 포맷은 이 클래스 밖으로 새어 나가지 않는다(부패 방지 계층). 외부 에러 코드 → 도메인
 * 예외(TossPaymentException) 매핑은 TossPaymentException.of 가 맡고, 타임아웃·연결 실패 → 도메인 예외 매핑은 이 클래스가 직접 맡는다.
 */
@Component
public class TossPaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(TossPaymentGateway.class);
    private static final String UNKNOWN_CODE = "UNKNOWN";

    private final RestClient tossRestClient;
    private final ObjectMapper objectMapper;

    public TossPaymentGateway(RestClient tossRestClient, ObjectMapper objectMapper) {
        this.tossRestClient = tossRestClient;
        this.objectMapper = objectMapper;
    }

    /**
     * 연결 단계 실패(ResourceAccessException)는 요청이 토스에 도달조차 못 했다는 뜻이라 재시도가 안전하다 — 최대 3회까지 재시도한다. 응답 읽기
     * 단계 실패(read timeout)는 요청이 이미 도달했을 수 있어 여기서 재시도하지 않고 PaymentResultUnknownException 으로 변환해, 호출부가 같은
     * 멱등키로 "재확인"하게 한다.
     */
    @Override
    @Retryable(retryFor = ResourceAccessException.class, maxAttempts = 3, backoff = @Backoff(delay = 200))
    public PaymentResult confirm(PaymentConfirmation confirmation) {
        ConfirmRequest request = new ConfirmRequest(
                confirmation.paymentKey(), confirmation.orderId(), confirmation.amount());
        try {
            TossPaymentResponse response = tossRestClient.post()
                    .uri("/v1/payments/confirm")
                    .contentType(MediaType.APPLICATION_JSON)
                    // orderId 를 멱등키로 그대로 쓴다. 결제 생성 시 한 번만 발급되어 재시도·새로고침에도 같은 값이 유지되므로
                    // (PaymentService.createOrder, generateOrderId 참고) 별도 컬럼 없이도 "주문당 고정 키" 조건을 만족한다.
                    .header("Idempotency-Key", confirmation.orderId())
                    .body(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, (req, res) -> {
                        throw TossPaymentException.of(res.getStatusCode(), readError(res));
                    })
                    .body(TossPaymentResponse.class);
            return toResult(response);
        } catch (ResourceAccessException e) {
            // @Retryable 이 재시도하도록 변환 없이 그대로 던진다. 여기서 다른 타입으로 감싸면 재시도 대상에서 빠진다.
            throw e;
        } catch (RestClientException e) {
            log.warn("Toss 응답을 읽지 못해 승인 결과가 불명확합니다. orderId={}", confirmation.orderId(), e);
            throw new PaymentResultUnknownException(
                    "결제 처리 결과를 확인하지 못했습니다. 같은 주문으로 다시 확인해주세요.", e);
        }
    }

    /**
     * 연결 재시도를 모두 소진했을 때 호출된다. 요청이 토스에 한 번도 도달하지 못했으므로 "확정된 실패"로 변환한다.
     */
    @Recover
    public PaymentResult recoverFromConnectionFailure(ResourceAccessException e, PaymentConfirmation confirmation) {
        log.warn("토스 연결에 반복 실패했습니다. orderId={}", confirmation.orderId(), e);
        throw new PaymentConnectionFailedException(
                "결제 서버에 연결할 수 없습니다. 잠시 후 다시 시도해주세요.", e);
    }

    /**
     * retryFor 에 해당하지 않는 예외(PaymentResultUnknownException, TossPaymentException 등)도 재시도 "소진" 경로를
     * 한 번 거친다 — Spring Retry 는 @Recover 메서드가 하나라도 있으면, 매칭되는 시그니처가 없는 예외를
     * ExhaustedRetryException 으로 감싸 버린다. 이 메서드가 그 catch-all 역할을 해 원래 예외를 그대로 던진다.
     */
    @Recover
    public PaymentResult propagateNonConnectionFailure(Exception e, PaymentConfirmation confirmation) throws Exception {
        throw e;
    }

    private PaymentResult toResult(TossPaymentResponse response) {
        return new PaymentResult(
                response.paymentKey(),
                response.orderId(),
                PaymentStatus.from(response.status()),
                response.totalAmount()
        );
    }

    /**
     * 에러 본문을 TossErrorResponse 로 역직렬화한다. 본문이 비었거나 JSON 이 아니어도 원래 실패를 삼키지 않도록 UNKNOWN 코드로 감싼다.
     */
    private TossErrorResponse readError(ClientHttpResponse res) throws IOException {
        byte[] body = res.getBody().readAllBytes();
        if (body.length == 0) {
            return new TossErrorResponse(UNKNOWN_CODE, "토스 에러 응답 본문이 비어 있습니다 (status=" + res.getStatusCode() + ")");
        }
        try {
            return objectMapper.readValue(body, TossErrorResponse.class);
        } catch (IOException e) {
            return new TossErrorResponse(UNKNOWN_CODE,
                    "토스 에러 응답을 해석할 수 없습니다: " + new String(body, StandardCharsets.UTF_8));
        }
    }
}
