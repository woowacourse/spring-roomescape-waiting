package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.Order;
import roomescape.domain.PaymentStatus;
import roomescape.domain.Reservation;
import roomescape.fixture.DbFixtures;
import roomescape.fixture.Fixtures;

class OrderServiceTest extends ServiceIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Test
    @DisplayName("예약으로 주문을 생성하면 주문을 같이 생성한다")
    void createPersistsOrder() {
        insertDefaultStore();
        long themeId = DbFixtures.insertTheme(jdbcTemplate, "테마");
        long timeId = DbFixtures.insertTime(jdbcTemplate, "10:00");
        long userId = DbFixtures.insertMember(jdbcTemplate, "브라운");
        long reservationId = DbFixtures.insertReservation(
                jdbcTemplate, userId, themeId, "2026-05-08", timeId, DEFAULT_STORE_ID);
        Reservation reservation = Fixtures.sampleReservation(reservationId);

        Order created = orderService.create(reservation);

        assertThat(created.getId()).isPositive();
        assertThat(created.getReservationId()).isEqualTo(reservationId);
        assertThat(created.getStatus()).isEqualTo(PaymentStatus.READY);
        assertThat(created.getAmount()).isEqualTo(50000L);
    }
}
