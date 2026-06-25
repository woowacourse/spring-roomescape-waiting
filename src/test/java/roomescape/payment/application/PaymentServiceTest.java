package roomescape.payment.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import roomescape.payment.domain.PaymentConfirmation;
import roomescape.payment.domain.PaymentGateway;
import roomescape.payment.domain.PaymentResult;
import roomescape.payment.domain.PaymentStatus;
import roomescape.payment.domain.exception.PaymentAmountMismatchException;
import roomescape.payment.domain.exception.PaymentGatewayResponseTimeoutException;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@Sql("/cleanup.sql")
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@Sql(statements = {
        "INSERT INTO store (id, name) VALUES (1, '강남점')",
        "INSERT INTO member (id, email, password, name, role) VALUES (1, 'brown@email.com', 'password', '브라운', 'USER')",
        "INSERT INTO theme (id, name, description, img_url) VALUES (1, '테마', '설명', 'https://example.com/theme.jpg')",
        "INSERT INTO reservation_time (id, start_at) VALUES (1, '10:00')"
})
class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoBean
    private PaymentGateway paymentGateway;

    @Test
    void 결제_전에는_주문과_PENDING_예약을_저장하고_이력은_남기지_않는다() {
        PaymentCheckout checkout = prepare();

        assertThat(checkout.orderId()).matches("[A-Za-z0-9_-]{6,64}");
        assertThat(checkout.amount()).isEqualTo(50_000L);
        assertThat(value("SELECT status FROM reservation WHERE id = 1", String.class)).isEqualTo("PENDING");
        assertThat(value("SELECT amount FROM payment_order WHERE reservation_id = 1", Long.class)).isEqualTo(50_000L);
        assertThat(value("SELECT COUNT(*) FROM reservation_history", Integer.class)).isZero();
    }

    @Test
    void 저장_금액과_다르면_승인_호출_전에_차단한다() {
        PaymentCheckout checkout = prepare();

        assertThatThrownBy(() -> paymentService.confirm(1L, "payment-key", checkout.orderId(), 49_000L))
                .isInstanceOf(PaymentAmountMismatchException.class);
        verify(paymentGateway, never()).confirm(any());
        assertThat(value("SELECT status FROM reservation WHERE id = 1", String.class)).isEqualTo("PENDING");
    }

    @Test
    void 승인에_성공하면_paymentKey를_저장하고_예약을_CONFIRMED로_바꾼다() {
        PaymentCheckout checkout = prepare();
        given(paymentGateway.confirm(any())).willReturn(
                new PaymentResult("payment-key", checkout.orderId(), PaymentStatus.DONE, 50_000L));

        PaymentResult result = paymentService.confirm(1L, "payment-key", checkout.orderId(), 50_000L);

        assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
        assertThat(value("SELECT status FROM reservation WHERE id = 1", String.class)).isEqualTo("CONFIRMED");
        assertThat(value("SELECT status FROM payment_order WHERE reservation_id = 1", String.class)).isEqualTo("DONE");
        assertThat(value("SELECT payment_key FROM payment_order WHERE reservation_id = 1", String.class))
                .isEqualTo("payment-key");
        assertThat(value("SELECT COUNT(*) FROM reservation_history", Integer.class)).isEqualTo(1);
        verify(paymentGateway).confirm(new PaymentConfirmation("payment-key", checkout.orderId(), 50_000L));
    }

    @Test
    void 같은_승인_콜백이_반복되면_저장된_성공을_반환하고_외부_승인은_반복하지_않는다() {
        PaymentCheckout checkout = prepare();
        given(paymentGateway.confirm(any())).willReturn(
                new PaymentResult("payment-key", checkout.orderId(), PaymentStatus.DONE, 50_000L));

        paymentService.confirm(1L, "payment-key", checkout.orderId(), 50_000L);
        PaymentResult retried = paymentService.confirm(1L, "payment-key", checkout.orderId(), 50_000L);

        assertThat(retried.status()).isEqualTo(PaymentStatus.DONE);
        verify(paymentGateway, times(1)).confirm(any());
        assertThat(value("SELECT COUNT(*) FROM reservation_history", Integer.class)).isEqualTo(1);
    }

    @Test
    void read_timeout이_재시도_끝까지_계속되면_PaymentOrder가_UNCONFIRMED로_저장되고_예외가_전파된다() {
        PaymentCheckout checkout = prepare();
        given(paymentGateway.confirm(any())).willThrow(new PaymentGatewayResponseTimeoutException());

        assertThatThrownBy(() -> paymentService.confirm(1L, "payment-key", checkout.orderId(), 50_000L))
                .isInstanceOf(PaymentGatewayResponseTimeoutException.class);

        verify(paymentGateway, times(3)).confirm(any());
        assertThat(value("SELECT status FROM payment_order WHERE reservation_id = 1", String.class))
                .isEqualTo("UNCONFIRMED");
        assertThat(value("SELECT payment_key FROM payment_order WHERE reservation_id = 1", String.class))
                .isEqualTo("payment-key");
        assertThat(value("SELECT status FROM reservation WHERE id = 1", String.class)).isEqualTo("PENDING");
        assertThat(value("SELECT COUNT(*) FROM reservation_history", Integer.class)).isZero();
    }

    @Test
    void UNCONFIRMED_주문에_같은_paymentKey로_재confirm하면_DONE으로_복구되고_예약은_CONFIRMED로_전환된다() {
        PaymentCheckout checkout = prepare();
        given(paymentGateway.confirm(any()))
                .willThrow(new PaymentGatewayResponseTimeoutException())
                .willThrow(new PaymentGatewayResponseTimeoutException())
                .willThrow(new PaymentGatewayResponseTimeoutException())
                .willReturn(new PaymentResult("payment-key", checkout.orderId(), PaymentStatus.DONE, 50_000L));

        assertThatThrownBy(() -> paymentService.confirm(1L, "payment-key", checkout.orderId(), 50_000L))
                .isInstanceOf(PaymentGatewayResponseTimeoutException.class);

        PaymentResult result = paymentService.confirm(1L, "payment-key", checkout.orderId(), 50_000L);

        assertThat(result.status()).isEqualTo(PaymentStatus.DONE);
        assertThat(value("SELECT status FROM payment_order WHERE reservation_id = 1", String.class)).isEqualTo("DONE");
        assertThat(value("SELECT status FROM reservation WHERE id = 1", String.class)).isEqualTo("CONFIRMED");
        assertThat(value("SELECT COUNT(*) FROM reservation_history", Integer.class)).isEqualTo(1);
    }

    @Test
    void fail의_orderId가_null이어도_예외가_발생하지_않는다() {
        assertThatCode(() -> paymentService.fail(1L, null)).doesNotThrowAnyException();
    }

    @Test
    void 결제_실패를_처리하면_PENDING_주문과_예약을_정리한다() {
        PaymentCheckout checkout = prepare();

        paymentService.fail(1L, checkout.orderId());

        assertThat(value("SELECT COUNT(*) FROM payment_order", Integer.class)).isZero();
        assertThat(value("SELECT COUNT(*) FROM reservation", Integer.class)).isZero();
    }

    private PaymentCheckout prepare() {
        return paymentService.prepareReservation(
                1L, LocalDate.of(2026, 12, 1), 1L, 1L, 1L);
    }

    private <T> T value(String sql, Class<T> type) {
        return jdbcTemplate.queryForObject(sql, type);
    }
}
