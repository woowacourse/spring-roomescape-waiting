package roomescape.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import roomescape.domain.Payment;
import roomescape.repository.PaymentJdbcRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(PaymentJdbcRepository.class)
@TestPropertySource(properties = "spring.sql.init.data-locations=")
class PaymentJdbcRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PaymentJdbcRepository repository;

    private Long reservationId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES ('10:00')");
        Long timeId = jdbcTemplate.queryForObject("SELECT id FROM reservation_time LIMIT 1", Long.class);
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_image_url) VALUES ('테마', '설명', 'url')");
        Long themeId = jdbcTemplate.queryForObject("SELECT id FROM theme LIMIT 1", Long.class);
        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, time_id, theme_id) VALUES ('민욱', '2026-06-20', ?, ?)",
                timeId, themeId
        );
        reservationId = jdbcTemplate.queryForObject("SELECT id FROM reservation LIMIT 1", Long.class);
    }

    @Test
    void save는_생성된_id를_부여하고_승인_전이라_paymentKey가_비어있는_결제를_반환한다() {
        Payment saved = repository.save(new Payment("order-abc123", 50_000L, reservationId));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getOrderId()).isEqualTo("order-abc123");
        assertThat(saved.getPaymentKey()).isNull();
    }

    @Test
    void findByOrderId는_저장된_결제를_반환한다() {
        repository.save(new Payment("order-abc123", 50_000L, reservationId));

        Optional<Payment> found = repository.findByOrderId("order-abc123");

        assertThat(found).isPresent();
        assertThat(found.get().getAmount()).isEqualTo(50_000L);
        assertThat(found.get().getReservationId()).isEqualTo(reservationId);
    }

    @Test
    void findByOrderId는_없는_주문이면_빈_Optional을_반환한다() {
        assertThat(repository.findByOrderId("order-none")).isEmpty();
    }
}
