package roomescape.feature.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.ResponseSpec.ErrorHandler;
import roomescape.feature.payment.config.TossPaymentProperties;
import roomescape.feature.payment.dto.PaymentApproveRequest;
import roomescape.feature.payment.dto.PaymentErrorResponse;
import roomescape.feature.payment.dto.PaymentResponse;

/**
 * 토스 결제 API와 통신하는 HTTP 클라이언트.
 *
 * baseUrl·인증을 이 클라이언트가 소유한다.
 * 전송 타임아웃(requestFactory)은 주입받은 {@code tossRestClientBuilder}에 이미 적용돼 있으며,
 * 이 클라이언트는 {@code requestFactory}를 호출하지 않는다 — {@code MockRestServiceServer} 테스트 seam을 보존하기 위함.
 */
@Component
public class TossPaymentClient {

    private static final String CONFIRM_PATH = "/v1/payments/confirm";
    private static final String PAYMENT_STATUS_PATH = "/v1/payments/{paymentKey}";
    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public TossPaymentClient(
            RestClient.Builder tossRestClientBuilder,
            TossPaymentProperties properties,
            ObjectMapper objectMapper
    ) {
        this.restClient = tossRestClientBuilder
                .baseUrl(properties.baseUrl())
                .defaultHeader(HttpHeaders.AUTHORIZATION, basicAuthorizationHeader(properties.secretKey()))
                .build();
        this.objectMapper = objectMapper;
    }

    /**
     * 결제 승인을 요청한다. Idempotency-Key 는 재시도마다 동일해야 하므로 주문 단위로 고정된 orderId 를 사용한다.
     * 토스가 에러 코드를 응답하면 {@link PaymentException} 으로 변환해 던진다.
     */
    public PaymentResponse confirm(PaymentApproveRequest request) {
        return restClient.post()
                .uri(CONFIRM_PATH)
                .header(IDEMPOTENCY_KEY_HEADER, request.orderId())
                .body(request)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::isError, errorHandler())
                .body(PaymentResponse.class);
    }

    /**
     * 결제 상태를 조회한다. (읽기 타임아웃 후 reconciliation 등에 사용)
     */
    public PaymentResponse findPayment(String paymentKey) {
        return restClient.get()
                .uri(PAYMENT_STATUS_PATH, paymentKey)
                .retrieve()
                .onStatus(HttpStatusCode::isError, errorHandler())
                .body(PaymentResponse.class);
    }

    private ErrorHandler errorHandler() {
        return (request, response) -> {
            PaymentErrorResponse errorResponse = objectMapper.readValue(response.getBody(), PaymentErrorResponse.class);
            PaymentFailureType failureType = PaymentFailureType.from(errorResponse.code());

            throw new PaymentException(failureType, errorResponse.code(), errorResponse.message());
        };
    }

    /**
     * 토스 인증 헤더 형식: Basic base64(secretKey + ":")
     * 시크릿 키 뒤에 콜론을 반드시 하나 붙여야 한다. (비밀번호 슬롯이 비어 있음을 의미)
     */
    private String basicAuthorizationHeader(String secretKey) {
        String credentials = secretKey + ":";
        String encoded = Base64.getEncoder()
                .encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }
}
