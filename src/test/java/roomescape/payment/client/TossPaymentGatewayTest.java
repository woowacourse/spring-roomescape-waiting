package roomescape.payment.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.io.UncheckedIOException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.HttpClientErrorException;
import roomescape.payment.PaymentConfirmation;
import roomescape.payment.PaymentStatus;


@SpringBootTest
class TossPaymentGatewayTest {

  static MockWebServer mockWebServer;

  static {
    mockWebServer = new MockWebServer();
    try {
      mockWebServer.start();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Autowired
  private TossPaymentGateway tossPaymentGateway;

  @DynamicPropertySource
  static void tossProperties(DynamicPropertyRegistry registry) {
    registry.add("toss.base-url", () -> mockWebServer.url("/").toString());
    registry.add("toss.secret-key", () -> "test_gsk_dummy");
  }

  @AfterAll
  static void tearDown() throws IOException {
    mockWebServer.shutdown();
  }

  private void enqueue(int statusCode, String body) {
    mockWebServer.enqueue(new MockResponse()
        .setResponseCode(statusCode)
        .setHeader("Content-Type", "application/json")
        .setBody(body));
  }

  @Test
  void confirm이_성공하면_status가_DONE인_결과를_반환한다() {
    enqueue(200, """
        {
          "paymentKey": "test_pk_1",
          "orderId": "order-1",
          "orderName": "방탈출 예약",
          "status": "DONE",
          "totalAmount": 10000,
          "balanceAmount": 10000,
          "method": "카드",
          "approvedAt": "2026-06-08T12:00:00+09:00",
          "requestedAt": "2026-06-08T11:59:30+09:00"
        }
        """);

    var result = tossPaymentGateway.confirm(
        new PaymentConfirmation("test_pk_1", "order-1", 10000L));

    assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
    assertThat(result.approvedAmount()).isEqualTo(10000L);
  }

  @Test
  void Toss가_4xx_응답을_반환하면_HttpClientErrorException이_던져진다() {
    enqueue(400, """
        {"code": "ALREADY_PROCESSED_PAYMENT", "message": "이미 처리된 결제 입니다."}
        """);

    assertThatThrownBy(() -> tossPaymentGateway.confirm(
        new PaymentConfirmation("test_pk_1", "order-1", 10000L)))
        .isInstanceOf(HttpClientErrorException.class);
  }

}
