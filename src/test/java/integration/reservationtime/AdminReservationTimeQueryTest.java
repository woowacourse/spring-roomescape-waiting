package integration.reservationtime;

import static org.assertj.core.api.Assertions.assertThat;

import integration.BaseIntegrationTest;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.controller.admin.api.dto.response.AdminReservationTimeResponse;
import roomescape.controller.admin.api.query.AdminReservationTimeQuery;
import roomescape.domain.TimeStatus;

class AdminReservationTimeQueryTest extends BaseIntegrationTest {

    @Autowired
    private AdminReservationTimeQuery adminReservationTimeQuery;
    @Autowired
    private ReservationTimeDataSource dataSource;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        dataSource.clearTable();
        dataSource.clearId();
    }

    @Test
    void 관리자_예약_시간_목록은_상태를_포함해_전체_시간을_조회한다() {
        // given
        jdbcTemplate.update(
                "INSERT INTO reservation_time (start_at, status) VALUES (?, ?)",
                LocalTime.of(10, 0),
                TimeStatus.ACTIVE.toString()
        );
        jdbcTemplate.update(
                "INSERT INTO reservation_time (start_at, status) VALUES (?, ?)",
                LocalTime.of(11, 0),
                TimeStatus.INACTIVE.toString()
        );

        // when
        List<AdminReservationTimeResponse> result = adminReservationTimeQuery.getAllReservationTimes();

        // then
        assertThat(result)
                .extracting(AdminReservationTimeResponse::startAt)
                .containsExactly(LocalTime.of(10, 0), LocalTime.of(11, 0));
        assertThat(result)
                .extracting(AdminReservationTimeResponse::status)
                .containsExactly("ACTIVE", "INACTIVE");
    }
}
