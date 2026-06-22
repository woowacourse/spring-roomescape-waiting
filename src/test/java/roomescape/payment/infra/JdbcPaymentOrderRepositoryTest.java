package roomescape.payment.infra;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.payment.domain.PaymentOrder;
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
                10_000L
        );
        repository.confirm("ROOM_order123", "payment-key");

        PaymentOrder confirmed = repository.findByOrderId("ROOM_order123").orElseThrow();
        assertThat(confirmed.status()).isEqualTo(PaymentOrderStatus.CONFIRMED);
        assertThat(confirmed.paymentKey()).isEqualTo("payment-key");
    }
}
