package roomescape.feature.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClient.ResponseSpec.ErrorHandler;
import roomescape.feature.payment.dto.PaymentApproveRequest;
import roomescape.feature.payment.dto.PaymentErrorResponse;
import roomescape.feature.payment.dto.PaymentResponse;

@RequiredArgsConstructor
@Component
public class PaymentApprover {

    private static final String APPROVE_URI = "/v1/payments/confirm";

    private final RestClient paymentRestClient;
    private final ObjectMapper objectMapper;

    public boolean approve(PaymentApproveRequest request) {
        try {
            PaymentResponse response = paymentRestClient.post()
                    .uri(APPROVE_URI)
                    .body(request)
                    .contentType(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, errorHandler())
                    .body(PaymentResponse.class);

            return response != null
                    && response.status() == PaymentStatus.DONE;
        } catch (PaymentException e) {
            if (e.getFailureType() == PaymentFailureType.ALREADY_DONE) {
                // 멱등: 이미 처리된 결제는 승인된 것으로 간주한다. (토스 승인 성공 후 DB 반영 실패 복구 등)
                return true;
            }
            throw e;
        }
    }

    private ErrorHandler errorHandler() {
        return (request, response) -> {
            PaymentErrorResponse errorResponse = objectMapper.readValue(response.getBody(), PaymentErrorResponse.class);
            PaymentFailureType failureType = PaymentFailureType.from(errorResponse.code());

            throw new PaymentException(failureType, errorResponse.code(), errorResponse.message());
        };
    }
}
