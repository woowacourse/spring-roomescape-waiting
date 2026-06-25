package roomescape.payment.client;

import java.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class TossPaymentClient {

  private final RestClient restClient;

  public TossPaymentClient(@Value("${toss.secret-key}") String secretKey) {
    String encoded = Base64.getEncoder().encodeToString((secretKey + ":").getBytes());
    this.restClient = RestClient.builder()
        .baseUrl("https://api.tosspayments.com")
        .defaultHeader("Authorization", "Basic " + encoded)
        .build();
  }

  public void confirm(String paymentKey, String orderId, Long amount) {
    restClient.post()
        .uri("/v1/payments/confirm")
        .contentType(MediaType.APPLICATION_JSON)
        .body(new TossConfirmRequest(paymentKey, orderId, amount))
        .retrieve()
        .toBodilessEntity();
  }

  private record TossConfirmRequest(String paymentKey, String orderId, Long amount) {}
}
