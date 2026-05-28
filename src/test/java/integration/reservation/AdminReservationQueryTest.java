package integration.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import integration.BaseIntegrationTest;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.controller.admin.api.dto.response.AdminReservationResponse;
import roomescape.controller.admin.api.query.AdminReservationQuery;

class AdminReservationQueryTest extends BaseIntegrationTest {

    @Autowired
    private AdminReservationQuery adminReservationQuery;
    @Autowired
    private ReservationDataSource dataSource;

    @BeforeEach
    void setUp() {
        dataSource.clearTable();
        dataSource.clearId();
        dataSource.insertTheme("공포", "어마무시한 공포 테마", "https://theme.com/image.png");
        dataSource.insertReservationTime(LocalTime.of(10, 0));
    }

    @Test
    void 관리자_예약_목록을_예약_엔트리_정보와_함께_조회한다() {
        // given
        LocalDate date = LocalDate.now().plusDays(1);
        dataSource.insertReservation("이프", date, 1L, 1L);

        // when
        List<AdminReservationResponse> result = adminReservationQuery.getAllReservations();

        // then
        assertThat(result).hasSize(1);
        AdminReservationResponse reservation = result.getFirst();
        assertThat(reservation.reservationId()).isEqualTo(1L);
        assertThat(reservation.date()).isEqualTo(date);
        assertThat(reservation.theme().name()).isEqualTo("공포");
        assertThat(reservation.time().startAt()).isEqualTo(LocalTime.of(10, 0));
        assertThat(reservation.entry().name()).isEqualTo("이프");
        assertThat(reservation.entry().status()).isEqualTo("RESERVED");
    }
}
