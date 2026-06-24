package roomescape.payment.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.ConnectException;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentResult;
import roomescape.payment.domain.PaymentStatus;
import roomescape.payment.dto.request.ConfirmRequest;
import roomescape.payment.dto.response.TossErrorResponse;
import roomescape.payment.dto.response.TossPaymentResponse;
import roomescape.payment.exception.PaymentGatewayConnectionException;
import roomescape.payment.exception.PaymentResultUnknownException;
import roomescape.payment.exception.TossPaymentException;
import roomescape.payment.port.PaymentGateway;

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
      ConfirmRequest request = new ConfirmRequest(confirmation.paymentKey(),
          confirmation.orderId(), confirmation.amount());
      var response = tossRestClient.post()
          .uri("/v1/payments/confirm")
          .contentType(MediaType.APPLICATION_JSON)
          .body(request)
          .retrieve()
          .onStatus(HttpStatusCode::isError, (req, res) -> {
            TossErrorResponse error = objectMapper.readValue(res.getBody(),
                TossErrorResponse.class);
            throw TossPaymentException.of(res.getStatusCode(), error);
          })
          .body(TossPaymentResponse.class);

      return toResult(response);
    } catch (ResourceAccessException e) {
      Throwable cause = e.getCause();
      if (cause instanceof ConnectException) {
        throw new PaymentGatewayConnectionException("결제사에 연결할 수 없습니다. 잠시 후 다시 시도해주세요.");
      }

      throw new PaymentResultUnknownException("결제 처리 결과를 확인할 수 없습니다. 결제 내역을 확인해주세요.");
    }
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
