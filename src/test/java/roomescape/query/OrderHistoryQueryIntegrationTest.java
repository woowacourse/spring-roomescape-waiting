package roomescape.query;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import roomescape.service.result.OrderHistoryResult;
import roomescape.support.IntegrationTest;

@IntegrationTest
@Sql("/integration-fixture.sql")
class OrderHistoryQueryIntegrationTest {

    @Autowired
    private ReservationQueryRepository reservationQueryRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("INSERT INTO reservation (id, date, theme_id, time_id) VALUES (1, '2025-01-02', 1, 1)");
        jdbcTemplate.update("INSERT INTO reservation (id, date, theme_id, time_id) VALUES (2, '2025-01-03', 1, 2)");

        // RESERVED 엔트리 + 결제 완료된 주문
        jdbcTemplate.update(
                "INSERT INTO reservation_entry (id, name, reservation_id, status, created_at) "
                        + "VALUES (1, '이프', 1, 'RESERVED', CURRENT_TIMESTAMP)");
        jdbcTemplate.update(
                "INSERT INTO payment_order (order_id, amount, entry_id, created_at, payment_key, status) "
                        + "VALUES ('order-1', 30000, 1, CURRENT_TIMESTAMP, 'pk-1', 'CONFIRMED')");

        // WAITING 엔트리 - 연결된 주문 없음
        jdbcTemplate.update(
                "INSERT INTO reservation_entry (id, name, reservation_id, status, created_at) "
                        + "VALUES (2, '이프', 2, 'WAITING', CURRENT_TIMESTAMP)");

        // 다른 사람의 엔트리 - 조회되면 안 됨
        jdbcTemplate.update(
                "INSERT INTO reservation_entry (id, name, reservation_id, status, created_at) "
                        + "VALUES (3, '두둠', 1, 'WAITING', CURRENT_TIMESTAMP)");
    }

    @Test
    void 이름으로_주문_내역을_결제_상태와_함께_조회하며_주문이_없는_엔트리는_결제정보가_null이다() {
        // when
        List<OrderHistoryResult> result = reservationQueryRepository.getOrderHistories("이프");

        // then
        assertThat(result).hasSize(2);

        OrderHistoryResult confirmed = result.stream()
                .filter(r -> "RESERVED".equals(r.entryStatus()))
                .findFirst()
                .orElseThrow();
        assertThat(confirmed.orderId()).isEqualTo("order-1");
        assertThat(confirmed.paymentKey()).isEqualTo("pk-1");
        assertThat(confirmed.amount()).isEqualTo(30000L);
        assertThat(confirmed.paymentStatus()).isEqualTo("CONFIRMED");
        assertThat(confirmed.themeName()).isEqualTo("공포");

        OrderHistoryResult waiting = result.stream()
                .filter(r -> "WAITING".equals(r.entryStatus()))
                .findFirst()
                .orElseThrow();
        assertThat(waiting.orderId()).isNull();
        assertThat(waiting.paymentKey()).isNull();
        assertThat(waiting.amount()).isNull();
        assertThat(waiting.paymentStatus()).isNull();
    }
}
