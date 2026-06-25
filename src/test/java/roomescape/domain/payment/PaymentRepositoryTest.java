package roomescape.domain.payment;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@JdbcTest
@Import(PaymentRepository.class)
class PaymentRepositoryTest {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void 주문의_멱등키와_결제_상태를_저장하고_조회한다() {
        Long reservationId = createReservation();
        ReservationPayment pending = ReservationPayment.pending(reservationId, 10_000L);

        ReservationPayment saved = paymentRepository.save(pending);
        ReservationPayment found = paymentRepository.findByOrderId(saved.orderId()).orElseThrow();

        assertThat(found.idempotencyKey()).isEqualTo(saved.idempotencyKey());
        assertThat(found.status()).isEqualTo(PaymentStatus.PENDING);
    }

    @Test
    void 승인_결과를_저장한다() {
        Long reservationId = createReservation();
        ReservationPayment saved = paymentRepository.save(
            ReservationPayment.pending(reservationId, 10_000L)
        );

        paymentRepository.updateConfirmed(saved.orderId(), "paymentKey", 10_000L);

        ReservationPayment confirmed = paymentRepository.findByOrderId(saved.orderId()).orElseThrow();
        assertThat(confirmed.paymentKey()).isEqualTo("paymentKey");
        assertThat(confirmed.status()).isEqualTo(PaymentStatus.CONFIRMED);
    }

    @Test
    void 결과가_불명확하면_paymentKey와_확인_필요_상태를_저장한다() {
        Long reservationId = createReservation();
        ReservationPayment saved = paymentRepository.save(
            ReservationPayment.pending(reservationId, 10_000L)
        );

        paymentRepository.updateRequiresConfirmation(saved.orderId(), "paymentKey");

        ReservationPayment unknown = paymentRepository.findByOrderId(saved.orderId()).orElseThrow();
        assertThat(unknown.paymentKey()).isEqualTo("paymentKey");
        assertThat(unknown.status()).isEqualTo(PaymentStatus.REQUIRES_CONFIRMATION);
    }

    private Long createReservation() {
        jdbcTemplate.update(
            "INSERT INTO reservation_time (start_at, finish_at) VALUES ('20:00:00', '21:00:00')"
        );
        jdbcTemplate.update(
            "INSERT INTO theme (name, description, image_url) VALUES ('결제테마', '설명', 'image')"
        );
        jdbcTemplate.update("""
            INSERT INTO reservation (name, date, time_id, theme_id)
            VALUES ('결제자', '2099-12-31',
                    (SELECT id FROM reservation_time WHERE start_at = '20:00:00'),
                    (SELECT id FROM theme WHERE name = '결제테마'))
            """);
        return jdbcTemplate.queryForObject(
            "SELECT id FROM reservation WHERE name = '결제자'",
            Long.class
        );
    }
}
