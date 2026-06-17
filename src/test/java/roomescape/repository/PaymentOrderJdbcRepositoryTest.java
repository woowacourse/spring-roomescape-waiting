package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.payment.PaymentOrder;
import roomescape.fixture.DbFixtures;

@JdbcTest
@Import(PaymentOrderJdbcRepository.class)
class PaymentOrderJdbcRepositoryTest {

    @Autowired
    private PaymentOrderJdbcRepository repository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void save_결제_주문을_저장하고_orderId로_조회한다() {
        Long userId = DbFixtures.insertMember(jdbcTemplate, "브라운");
        Long themeId = DbFixtures.insertTheme(jdbcTemplate, "공포");
        Long timeId = DbFixtures.insertTime(jdbcTemplate, "10:00");
        Long reservationId = DbFixtures.insertReservation(jdbcTemplate, userId, themeId, "2026-05-08", timeId,
                "PAYMENT_PENDING");

        Long id = repository.save(new PaymentOrder(null, reservationId, "order_123456", 37_000L));

        PaymentOrder found = repository.findByOrderId("order_123456").orElseThrow();
        assertThat(id).isPositive();
        assertThat(found.getReservationId()).isEqualTo(reservationId);
        assertThat(found.getOrderId()).isEqualTo("order_123456");
        assertThat(found.getAmount()).isEqualTo(37_000L);
        assertThat(found.getPaymentKey()).isNull();
    }

    @Test
    void updatePaymentKey_orderId에_해당하는_주문에_paymentKey를_저장한다() {
        Long userId = DbFixtures.insertMember(jdbcTemplate, "브라운");
        Long themeId = DbFixtures.insertTheme(jdbcTemplate, "공포");
        Long timeId = DbFixtures.insertTime(jdbcTemplate, "10:00");
        Long reservationId = DbFixtures.insertReservation(jdbcTemplate, userId, themeId, "2026-05-08", timeId,
                "PAYMENT_PENDING");
        repository.save(new PaymentOrder(null, reservationId, "order_123456", 37_000L));

        int affected = repository.updatePaymentKey("order_123456", "payment_key");

        PaymentOrder found = repository.findByOrderId("order_123456").orElseThrow();
        assertThat(affected).isEqualTo(1);
        assertThat(found.getPaymentKey()).isEqualTo("payment_key");
    }
}
