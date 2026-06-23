package roomescape.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.domain.Member;
import roomescape.domain.Order;
import roomescape.domain.OrderStatus;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.infrastructure.repository.OrderJdbcRepository;

@JdbcTest
@Import(OrderJdbcRepository.class)
class OrderJdbcRepositoryTest {

    private static final LocalDate RESERVATION_DATE = LocalDate.of(2026, 8, 5);
    private static final LocalTime RESERVATION_START_AT = LocalTime.of(10, 0);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private OrderJdbcRepository repository;

    private Long timeId;
    private Long themeId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update(
                "INSERT INTO reservation_time (start_at) VALUES (?)",
                RESERVATION_START_AT
        );
        timeId = jdbcTemplate.queryForObject(
                "SELECT id FROM reservation_time WHERE start_at = ?",
                Long.class,
                RESERVATION_START_AT
        );

        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_image_url) VALUES (?, ?, ?)",
                "공포",
                "무서운 테마",
                "https://example.com/horror.jpg"
        );
        themeId = jdbcTemplate.queryForObject(
                "SELECT id FROM theme WHERE name = ?",
                Long.class,
                "공포"
        );
    }

    @Test
    void 주문을_저장하고_조회한다() {
        Order order = order();

        repository.save(order);

        Order found = repository.findById(order.getOrderId()).orElseThrow();
        assertThat(found.getOrderId()).isEqualTo(order.getOrderId());
        assertThat(found.getAmount()).isEqualTo(50_000L);
        assertThat(found.getReservation().getName()).isEqualTo("민욱");
        assertThat(found.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(found.getPaymentKey()).isNull();
    }

    @Test
    void 결제_확정_주문을_수정하면_paymentKey와_상태를_갱신한다() {
        Order order = order();
        repository.save(order);

        repository.update(order.confirm("payment-key"));

        Order found = repository.findById(order.getOrderId()).orElseThrow();
        assertThat(found.getPaymentKey()).isEqualTo("payment-key");
        assertThat(found.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    }

    private Order order() {
        ReservationTime time = new ReservationTime(timeId, RESERVATION_START_AT);
        Theme theme = new Theme(
                themeId,
                "공포",
                "무서운 테마",
                "https://example.com/horror.jpg"
        );
        Reservation reservation = new Reservation(
                new Member("민욱"),
                new Slot(RESERVATION_DATE, time, theme)
        );

        return new Order(
                "order-id",
                "공포 예약",
                50_000L,
                reservation
        );
    }
}
