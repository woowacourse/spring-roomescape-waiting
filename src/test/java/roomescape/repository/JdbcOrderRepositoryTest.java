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
import roomescape.domain.payment.Order;
import roomescape.domain.payment.OrderStatus;
import roomescape.infra.persistence.JdbcOrderRepository;

@JdbcTest
@Sql(scripts = "/test-setup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class JdbcOrderRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private JdbcOrderRepository jdbcOrderRepository;

    @BeforeEach
    void setUp() {
        jdbcOrderRepository = new JdbcOrderRepository(jdbcTemplate);
    }

    @Test
    @DisplayName("주문 정보를 저장하고 영속화된 객체를 반환한다.")
    void 주문_저장() {
        Long reservationId = insertReservation();
        Order order = new Order("order-1", 50000L, reservationId);

        Order savedOrder = jdbcOrderRepository.save(order);

        assertThat(savedOrder.getId()).isPositive();
        assertThat(savedOrder.getOrderId()).isEqualTo("order-1");
        assertThat(savedOrder.getAmount()).isEqualTo(50000L);
        assertThat(savedOrder.getReservationId()).isEqualTo(reservationId);
        assertThat(savedOrder.getStatus()).isEqualTo(OrderStatus.PENDING);
    }

    @Test
    @DisplayName("주문 번호로 주문 정보를 조회한다.")
    void 주문_번호로_주문_조회() {
        Long reservationId = insertReservation();
        jdbcOrderRepository.save(new Order("order-1", 50000L, reservationId));

        Optional<Order> foundOrder = jdbcOrderRepository.findByOrderId("order-1");

        assertThat(foundOrder).isPresent();
        assertThat(foundOrder.get().getAmount()).isEqualTo(50000L);
        assertThat(foundOrder.get().getReservationId()).isEqualTo(reservationId);
    }

    @Test
    @DisplayName("존재하지 않는 주문 번호로 조회하면 빈 값을 반환한다.")
    void 존재하지_않는_주문_번호_조회() {
        Optional<Order> foundOrder = jdbcOrderRepository.findByOrderId("not-exists");

        assertThat(foundOrder).isEmpty();
    }

    @Test
    @DisplayName("주문 번호로 주문 상태를 업데이트한다.")
    void 주문_상태_업데이트() {
        Long reservationId = insertReservation();
        jdbcOrderRepository.save(new Order("order-1", 50000L, reservationId));

        Order updatedOrder = jdbcOrderRepository.updateStatus("order-1", OrderStatus.CONFIRMED);

        assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
        assertThat(updatedOrder.getOrderId()).isEqualTo("order-1");
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
