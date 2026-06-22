package roomescape.payment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import io.restassured.RestAssured;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import roomescape.payment.application.exception.PaymentErrorCode;
import roomescape.payment.application.exception.PaymentException;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentGateway;
import roomescape.payment.domain.PaymentOrder;
import roomescape.payment.domain.PaymentOrderStatus;
import roomescape.payment.domain.PaymentResult;
import roomescape.payment.infra.JdbcPaymentOrderRepository;
import roomescape.support.ApiIntegrationTestHelper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class PaymentApiIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private PaymentGateway paymentGateway;

    private JdbcPaymentOrderRepository paymentOrderRepository;

    @BeforeEach
    void setUp() {
        ApiIntegrationTestHelper helper = new ApiIntegrationTestHelper(jdbcTemplate);
        helper.clearDatabase();
        Long themeId = helper.insertTheme("테마", "설명", "이미지");
        Long timeId = helper.insertReservationTime(LocalTime.of(10, 0));

        paymentOrderRepository = new JdbcPaymentOrderRepository(jdbcTemplate);
        paymentOrderRepository.savePending(
                "카야",
                LocalDate.of(2028, 5, 6),
                themeId,
                timeId,
                "ROOM_order123",
                10_000L,
                "fixed-idempotency-key"
        );
    }

    @Test
    void read_timeout_후_확인_필요로_표시하고_같은_멱등키로_재시도한다() {
        PaymentConfirmation confirmation = new PaymentConfirmation(
                "payment-key",
                "ROOM_order123",
                10_000L
        );
        given(paymentGateway.confirm(confirmation, "fixed-idempotency-key"))
                .willThrow(new PaymentException(PaymentErrorCode.CONFIRMATION_UNKNOWN))
                .willReturn(new PaymentResult(
                        "payment-key",
                        "ROOM_order123",
                        10_000L,
                        "DONE"
                ));

        RestAssured.given()
                .port(port)
                .redirects().follow(false)
                .queryParam("paymentKey", "payment-key")
                .queryParam("orderId", "ROOM_order123")
                .queryParam("amount", 10_000L)
                .when().get("/payments/success")
                .then()
                .statusCode(302);

        PaymentOrder unknown = paymentOrderRepository.findByOrderId("ROOM_order123").orElseThrow();
        assertThat(unknown.status()).isEqualTo(PaymentOrderStatus.CONFIRMATION_UNKNOWN);
        assertThat(unknown.paymentKey()).isEqualTo("payment-key");

        RestAssured.given()
                .port(port)
                .redirects().follow(false)
                .queryParam("orderId", "ROOM_order123")
                .when().get("/payments/retry")
                .then()
                .statusCode(302);

        PaymentOrder confirmed = paymentOrderRepository.findByOrderId("ROOM_order123").orElseThrow();
        assertThat(confirmed.status()).isEqualTo(PaymentOrderStatus.CONFIRMED);
        verify(paymentGateway, org.mockito.Mockito.times(2))
                .confirm(confirmation, "fixed-idempotency-key");
    }
}
