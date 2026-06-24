package roomescape.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.domain.payment.Payment;
import roomescape.infra.persistence.JdbcPaymentRepository;

@JdbcTest
@Sql(scripts = "/test-setup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class JdbcPaymentRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private JdbcPaymentRepository jdbcPaymentRepository;

    @BeforeEach
    void setUp() {
        jdbcPaymentRepository = new JdbcPaymentRepository(jdbcTemplate);
    }

    @Test
    @DisplayName("승인된 결제 정보를 저장하고 영속화된 객체를 반환한다.")
    void 결제_저장() {
        insertOrder();
        Payment payment = new Payment("payment-key", "order-1");

        Payment savedPayment = jdbcPaymentRepository.save(payment);

        assertThat(savedPayment.getId()).isPositive();
        assertThat(savedPayment.getPaymentKey()).isEqualTo("payment-key");
        assertThat(savedPayment.getOrderId()).isEqualTo("order-1");
    }

    @Test
    @DisplayName("주문 번호로 승인된 결제 정보를 조회한다.")
    void 주문_번호로_결제_조회() {
        insertOrder();
        jdbcPaymentRepository.save(new Payment("payment-key", "order-1"));

        Optional<Payment> foundPayment = jdbcPaymentRepository.findByOrderId("order-1");

        assertThat(foundPayment).isPresent();
        assertThat(foundPayment.get().getPaymentKey()).isEqualTo("payment-key");
    }

    @Test
    @DisplayName("존재하지 않는 주문 번호로 조회하면 빈 값을 반환한다.")
    void 존재하지_않는_주문_번호_조회() {
        Optional<Payment> foundPayment = jdbcPaymentRepository.findByOrderId("not-exists");

        assertThat(foundPayment).isEmpty();
    }

    private void insertOrder() {
        Long reservationId = insertReservation();
        jdbcTemplate.update(
                "INSERT INTO orders (order_id, amount, reservation_id, status) VALUES (?, ?, ?, ?)",
                "order-1", 50000L, reservationId, "PENDING"
        );
    }

    private Long insertReservation() {
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url, price) VALUES (?, ?, ?, ?)",
                "테마", "설명", "thumbnail.png", 50000L
        );
        jdbcTemplate.update("INSERT INTO time_slot (start_at) VALUES (?)", "10:00:00");
        jdbcTemplate.update(
                "INSERT INTO reservation_slot (date, time_id, theme_id) VALUES (?, ?, ?)",
                LocalDate.now().plusDays(1), 1L, 1L
        );
        jdbcTemplate.update(
                "INSERT INTO reservation (name, slot_id, created_at, status) VALUES (?, ?, ?, ?)",
                "브라운", 1L, LocalDate.now().atStartOfDay(), "RESERVED"
        );

        return jdbcTemplate.queryForObject("SELECT id FROM reservation WHERE name = ?", Long.class, "브라운");
    }
}
