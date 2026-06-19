package roomescape.payment.client;

import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentGateway;
import roomescape.payment.PaymentResult;
import roomescape.payment.PaymentStatus;
import roomescape.payment.client.dto.ConfirmRequest;
import roomescape.payment.client.dto.TossErrorResponse;
import roomescape.payment.client.dto.TossPaymentResponse;

/**
 * PaymentGateway 포트의 Toss 구현(어댑터). Toss 의 요청·응답·에러 포맷은 이 클래스 밖으로 새어 나가지 않는다(부패 방지 계층).
 */
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
    var request = new ConfirmRequest(
        confirmation.paymentKey(), confirmation.orderId(), confirmation.amount());
    var response = tossRestClient.post()
        .uri("/v1/payments/confirm")
        .contentType(MediaType.APPLICATION_JSON)
        .body(request)
        .retrieve()
        .onStatus(HttpStatusCode::isError, (req, res) -> {
          var error = objectMapper.readValue(res.getBody(), TossErrorResponse.class);
          throw TossPaymentException.of(res.getStatusCode(), error);
        })
        .body(TossPaymentResponse.class);
    return toResult(response);
  }

  private PaymentResult toResult(TossPaymentResponse response) {
    return new PaymentResult(
        response.paymentKey(),
        response.orderId(),
        response.totalAmount(),
        PaymentStatus.from(response.status())
    );
  }

}
