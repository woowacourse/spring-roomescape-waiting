package roomescape.payment.adapter;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentResult;
import roomescape.payment.domain.PaymentStatus;
import roomescape.payment.dto.request.ConfirmRequest;
import roomescape.payment.dto.response.TossErrorResponse;
import roomescape.payment.dto.response.TossPaymentResponse;
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
