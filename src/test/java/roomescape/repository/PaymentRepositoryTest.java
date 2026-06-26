package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.Payment;
import roomescape.domain.PaymentStatus;

@JdbcTest
class PaymentRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private PaymentRepository paymentRepository;

    @BeforeEach
    void setUp() {
        paymentRepository = new PaymentRepository(jdbcTemplate);
        jdbcTemplate.update("DELETE FROM payment;");
        jdbcTemplate.update("DELETE FROM reservation;");
        jdbcTemplate.update("ALTER TABLE payment ALTER COLUMN id RESTART WITH 1;");
        jdbcTemplate.update("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1;");
    }

    @Test
    void 결제_대기_정보를_저장하고_조회한다() {
        Long reservationId = createPendingReservation();
        Payment payment = Payment.ready(reservationId, 20_000L);

        Payment savedPayment = paymentRepository.insert(payment);

        Payment foundPayment = paymentRepository.findById(savedPayment.getId()).orElseThrow();
        assertThat(foundPayment.getReservationId()).isEqualTo(reservationId);
        assertThat(foundPayment.getOrderId()).isEqualTo(payment.getOrderId());
        assertThat(foundPayment.getAmount()).isEqualTo(20_000L);
        assertThat(foundPayment.getStatus()).isEqualTo(PaymentStatus.READY);
        assertThat(paymentRepository.findByOrderId(payment.getOrderId()).orElseThrow().getId())
                .isEqualTo(foundPayment.getId());
    }

    @Test
    void 예약의_가장_최근_결제를_조회한다() {
        Long reservationId = createPendingReservation();
        paymentRepository.insert(Payment.ready(reservationId, 20_000L).fail("REJECT_CARD_PAYMENT", "카드 거절"));
        Payment latestPayment = paymentRepository.insert(Payment.ready(reservationId, 20_000L));

        assertThat(paymentRepository.findLatestByReservationId(reservationId).orElseThrow().getId())
                .isEqualTo(latestPayment.getId());
    }

    private Long createPendingReservation() {
        jdbcTemplate.update("""
                INSERT INTO reservation(name, date, time_id, theme_id, status)
                VALUES (?, ?, ?, ?, ?)
                """, "브라운", LocalDate.of(2099, 1, 1), 1L, 1L, "PENDING");
        return jdbcTemplate.queryForObject("SELECT id FROM reservation", Long.class);
    }
}
