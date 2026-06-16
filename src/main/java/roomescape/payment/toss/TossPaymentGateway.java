package roomescape.payment.toss;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import roomescape.domain.payment.PaymentConfirmation;
import roomescape.domain.payment.PaymentGateway;
import roomescape.domain.payment.PaymentResult;

/**
 * 토스 결제 승인 어댑터(부패 방지 계층). 토스 요청·응답·에러 *포맷(DTO)*은 이 클래스 밖으로 새지 않는다.
 * 에러는 onStatus에서 TossPaymentException으로 변환되어 호출부로 전파된다.
 */
@Component
public class TossPaymentGateway implements PaymentGateway {

    private final RestClient restClient;
    private final ObjectMapper objectMapper;
    private final TossProperties properties;
    private final String authorizationHeader;

    public TossPaymentGateway(RestClient.Builder restClientBuilder, ObjectMapper objectMapper,
                              TossProperties properties) {
        this.restClient = restClientBuilder.build();
        this.objectMapper = objectMapper;
        this.properties = properties;
        this.authorizationHeader = buildBasicAuthHeader(properties.secretKey());
    }

    @Override
    public PaymentResult confirm(PaymentConfirmation confirmation) {
        TossConfirmRequest request = new TossConfirmRequest(
                confirmation.paymentKey(), confirmation.orderId(), confirmation.amount());

        TossPaymentResponse response = restClient.post()
                .uri(properties.confirmUrl())
                .header(HttpHeaders.AUTHORIZATION, authorizationHeader)
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    TossErrorResponse error = objectMapper.readValue(res.getBody(), TossErrorResponse.class);
                    throw TossPaymentException.of(res.getStatusCode(), error);
                })
                .body(TossPaymentResponse.class);

        if (response == null) {
            throw new TossPaymentException(HttpStatus.INTERNAL_SERVER_ERROR, "EMPTY_RESPONSE",
                    "결제 응답이 비어 있습니다.");
        }
        return new PaymentResult(response.paymentKey(), response.orderId(),
                response.status(), response.totalAmount());
    }

    @Override
    public String clientKey() {
        return properties.clientKey();
    }

    /**
     * Basic 인증: base64(시크릿키 + ":"). 콜론 뒤 비밀번호는 비우고 UTF-8로 인코딩한다.
     */
    private static String buildBasicAuthHeader(String secretKey) {
        String raw = secretKey + ":";
        String encoded = Base64.getEncoder().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
        return "Basic " + encoded;
    }
}
