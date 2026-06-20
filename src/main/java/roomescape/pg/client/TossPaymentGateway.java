package roomescape.pg.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import roomescape.domain.payment.PaymentStatus;
import roomescape.pg.PaymentConfirmation;
import roomescape.pg.PaymentGateway;
import roomescape.pg.PaymentGatewayResult;
import roomescape.pg.PaymentResult;
import roomescape.pg.client.dto.ConfirmRequest;
import roomescape.pg.client.dto.TossErrorResponse;
import roomescape.pg.client.dto.TossPaymentResponse;

/**
 * PaymentGateway 포트의 Toss 구현(어댑터). Toss 의 요청·응답·에러 포맷은 이 클래스 밖으로 새어 나가지 않는다(부패 방지 계층).
 */
@Component
@Slf4j
public class TossPaymentGateway implements PaymentGateway {

  private final RestClient tossRestClient;
  private final ObjectMapper objectMapper;

  public TossPaymentGateway(RestClient tossRestClient, ObjectMapper objectMapper) {
    this.tossRestClient = tossRestClient;
    this.objectMapper = objectMapper;
  }

  @Override
  public PaymentGatewayResult confirm(PaymentConfirmation confirmation) {
    var request = new ConfirmRequest(confirmation.paymentKey(), confirmation.orderId(), confirmation.amount());
    try {
      var response = tossRestClient.post()
          .uri("/v1/payments/confirm")
          .contentType(MediaType.APPLICATION_JSON)
          .header("Idempotency-Key", confirmation.orderId())
          .body(request)
          .retrieve()
          .onStatus(HttpStatusCode::isError, (req, res) -> {
            var error = objectMapper.readValue(res.getBody(), TossErrorResponse.class);
            throw TossPaymentException.of(error);
          })
          .body(TossPaymentResponse.class);
      return new PaymentGatewayResult.Approved(toResult(response));
    } catch (RestClientException e) {
      log.error("Failed to confirm payment request", e);
      return unknown();
    } catch (OutboundRateLimitException e) {
      log.error("Skipped payment request by outbound rate limit", e);
      return unknown();
    } catch (TossPaymentException e) {
      log.error("Failed to confirm payment request", e);
      return handleTossException(e);
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

  private PaymentGatewayResult handleTossException(TossPaymentException exception) {
    if (exception instanceof TossPaymentException.Retryable) {
      return new PaymentGatewayResult.Unknown(exception.getMessage());
    }
    return new PaymentGatewayResult.Rejected(exception.getCode(), exception.getMessage());
  }

  private PaymentGatewayResult unknown() {
    return new PaymentGatewayResult.Unknown("결제 승인 결과를 확인할 수 없습니다.");
  }

}
