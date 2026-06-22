package roomescape.payment.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.SocketTimeoutException;
import java.util.Locale;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import roomescape.payment.*;
import roomescape.payment.client.dto.TossConfirmRequest;
import roomescape.payment.client.dto.TossErrorResponse;
import roomescape.payment.client.dto.TossPaymentResponse;

@Component
public class TossPaymentGateway implements PaymentGateway {

    private final RestClient tossRestClient;
    private final ObjectMapper objectMapper;

    public TossPaymentGateway(RestClient tossRestClient, ObjectMapper objectMapper) {
        this.tossRestClient = tossRestClient;
        this.objectMapper = objectMapper;
    }

    @Override
    public PaymentResult confirm(PaymentConfirmation confirmation) {
        try {
            TossPaymentResponse response = tossRestClient.post()
                    .uri("/v1/payments/confirm")
                    // 타임아웃으로 끊긴 뒤 재시도해도 같은 결제로 식별되도록 주문에 고정된 멱등키를 싣는다.
                    .header("Idempotency-Key", confirmation.idempotencyKey())
                    .body(new TossConfirmRequest(confirmation.paymentKey(), confirmation.orderId(), confirmation.amount()))
                    .retrieve()
                    .body(TossPaymentResponse.class);
            return response.toResult();
        } catch (RestClientResponseException e) {
            // 토스가 HTTP 에러 + {code, message} 로 명확히 거절한 경우 ("거절")
            TossErrorResponse error = parseError(e.getResponseBodyAsString());
            throw new TossPaymentException(error.message());
        } catch (ResourceAccessException e) {
            // I/O 단계 실패. 인터셉터가 응답 상태를 읽으면서 read timeout 도 여기로 들어오므로, 원인으로 구분한다.
            if (isReadTimeout(e)) {
                // 요청은 토스에 닿았으나 응답만 유실 → 승인 여부 불명 ("확인 필요")
                throw new PaymentResultUnknownException("결제 결과를 확인하지 못했습니다. 결제 내역에서 결과를 확인해주세요.", e);
            }
            // 연결 단계 실패(연결 거부/연결 타임아웃). 요청이 토스에 닿지 못함 → 재시도 가능 ("답 없음")
            throw new PaymentConnectionException("결제 서버에 연결할 수 없습니다. 잠시 후 다시 시도해주세요.", e);
        } catch (RestClientException e) {
            // 응답은 받았으나 본문 처리 등에서 실패 → 승인 여부 불명 ("확인 필요")
            throw new PaymentResultUnknownException("결제 결과를 확인하지 못했습니다. 결제 내역에서 결과를 확인해주세요.", e);
        }
    }

    private boolean isReadTimeout(ResourceAccessException e) {
        // connect 단계 타임아웃 메시지는 "connect timed out", read 단계는 "Read timed out" 으로 구분된다.
        Throwable cause = e.getCause();
        return cause instanceof SocketTimeoutException
                && cause.getMessage() != null
                && cause.getMessage().toLowerCase(Locale.ROOT).contains("read");
    }

    private TossErrorResponse parseError(String body) {
        try {
            return objectMapper.readValue(body, TossErrorResponse.class);
        } catch (Exception e) {
            return new TossErrorResponse("UNKNOWN", "결제 처리 중 오류가 발생했습니다.");
        }
    }
}
