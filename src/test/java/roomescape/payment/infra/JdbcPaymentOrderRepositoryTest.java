package roomescape.payment.infra;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.payment.domain.PaymentOrder;
import roomescape.payment.domain.PaymentOrderDetail;
import roomescape.payment.domain.PaymentOrderStatus;
import roomescape.support.RepositoryTestHelper;

@JdbcTest
class JdbcPaymentOrderRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void 결제_대기_주문을_승인하면_paymentKey와_CONFIRMED_상태를_저장한다() {
        RepositoryTestHelper helper = new RepositoryTestHelper(jdbcTemplate);
        Long themeId = helper.insertTheme("테마", "설명", "이미지");
        Long timeId = helper.insertReservationTime(java.time.LocalTime.of(10, 0));
        JdbcPaymentOrderRepository repository = new JdbcPaymentOrderRepository(jdbcTemplate);

        repository.savePending(
                "카야",
                LocalDate.of(2028, 5, 6),
                themeId,
                timeId,
                "ROOM_order123",
                10_000L,
                "fixed-idempotency-key"
        );
        repository.confirm("ROOM_order123", "payment-key");

        PaymentOrder confirmed = repository.findByOrderId("ROOM_order123").orElseThrow();
        assertThat(confirmed.status()).isEqualTo(PaymentOrderStatus.CONFIRMED);
        assertThat(confirmed.paymentKey()).isEqualTo("payment-key");
        assertThat(confirmed.idempotencyKey()).isEqualTo("fixed-idempotency-key");
    }

    @Test
    void 응답을_읽지_못한_주문은_확인_필요_상태와_paymentKey를_저장한다() {
        RepositoryFixture fixture = fixture();

        fixture.repository().markConfirmationUnknown("ROOM_order123", "payment-key");

        PaymentOrder order = fixture.repository().findByOrderId("ROOM_order123").orElseThrow();
        assertThat(order.status()).isEqualTo(PaymentOrderStatus.CONFIRMATION_UNKNOWN);
        assertThat(order.paymentKey()).isEqualTo("payment-key");
    }

    @Test
    void 이름으로_예약_정보와_결제_상태를_함께_조회한다() {
        RepositoryFixture fixture = fixture();
        fixture.repository().confirm("ROOM_order123", "payment-key");

        List<PaymentOrderDetail> histories = fixture.repository().findAllByName("카야");

        assertThat(histories).singleElement()
                .satisfies(history -> {
                    assertThat(history.themeName()).isEqualTo("테마");
                    assertThat(history.startAt()).isEqualTo(java.time.LocalTime.of(10, 0));
                    assertThat(history.orderId()).isEqualTo("ROOM_order123");
                    assertThat(history.status()).isEqualTo(PaymentOrderStatus.CONFIRMED);
                    assertThat(history.paymentKey()).isEqualTo("payment-key");
                    assertThat(history.amount()).isEqualTo(10_000L);
                });
    }

    private RepositoryFixture fixture() {
        RepositoryTestHelper helper = new RepositoryTestHelper(jdbcTemplate);
        Long themeId = helper.insertTheme("테마", "설명", "이미지");
        Long timeId = helper.insertReservationTime(java.time.LocalTime.of(10, 0));
        JdbcPaymentOrderRepository repository = new JdbcPaymentOrderRepository(jdbcTemplate);
        repository.savePending(
                "카야",
                LocalDate.of(2028, 5, 6),
                themeId,
                timeId,
                "ROOM_order123",
                10_000L,
                "fixed-idempotency-key"
        );
        return new RepositoryFixture(repository);
    }

    private record RepositoryFixture(JdbcPaymentOrderRepository repository) {
    }
}
