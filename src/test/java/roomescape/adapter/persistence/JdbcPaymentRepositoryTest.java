package roomescape.adapter.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.sql.Date;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.payment.Payment;
import roomescape.domain.payment.PaymentStatus;

@JdbcTest
@Import(JdbcPaymentRepository.class)
class JdbcPaymentRepositoryTest {

    @Autowired
    private JdbcPaymentRepository paymentRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long reservationId;

    @BeforeEach
    void setUp() {
        Long timeId = insertTime(LocalTime.of(10, 0));
        Long themeId = insertTheme("테마A");
        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id, status) VALUES (?, ?, ?, ?, ?)",
                "모카", Date.valueOf(LocalDate.now().plusDays(1)), timeId, themeId, "PENDING");
        reservationId = jdbcTemplate.queryForObject("SELECT MAX(id) FROM reservation", Long.class);
    }

    @Test
    void 대기_결제를_저장하고_orderId로_조회한다() {
        paymentRepository.save(Payment.pending(reservationId, "order_abc123", 1000L));

        Payment found = paymentRepository.findByOrderId("order_abc123").orElseThrow();

        assertThat(found.getAmount()).isEqualTo(1000L);
        assertThat(found.isPending()).isTrue();
        assertThat(found.getPaymentKey()).isNull();
    }

    @Test
    void 승인_결과를_반영하면_paymentKey와_상태가_갱신된다() {
        paymentRepository.save(Payment.pending(reservationId, "order_abc123", 1000L));

        paymentRepository.updateConfirmed("order_abc123", "test_pk_1", PaymentStatus.DONE);

        Payment found = paymentRepository.findByOrderId("order_abc123").orElseThrow();
        assertThat(found.getPaymentKey()).isEqualTo("test_pk_1");
        assertThat(found.getStatus()).isEqualTo(PaymentStatus.DONE);
    }

    private Long insertTime(LocalTime startAt) {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", Time.valueOf(startAt));
        return jdbcTemplate.queryForObject(
                "SELECT id FROM reservation_time WHERE start_at = ?",
                Long.class, Time.valueOf(startAt));
    }

    private Long insertTheme(String name) {
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)",
                name, "설명", "url");
        return jdbcTemplate.queryForObject(
                "SELECT id FROM theme WHERE name = ?", Long.class, name);
    }
}
