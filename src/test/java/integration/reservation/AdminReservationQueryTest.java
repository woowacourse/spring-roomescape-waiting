package integration.reservation;

import static org.assertj.core.api.Assertions.assertThat;

import integration.BaseIntegrationTest;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.controller.admin.api.dto.response.AdminReservationSlotResponse;
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
        List<AdminReservationSlotResponse> result = adminReservationQuery.getAllReservations();

        // then
        assertThat(result).hasSize(1);
        AdminReservationSlotResponse slot = result.getFirst();
        assertThat(slot.slotId()).isEqualTo(1L);
        assertThat(slot.date()).isEqualTo(date);
        assertThat(slot.theme().name()).isEqualTo("공포");
        assertThat(slot.time().startAt()).isEqualTo(LocalTime.of(10, 0));
        assertThat(slot.reservation().name()).isEqualTo("이프");
        assertThat(slot.reservation().status()).isEqualTo("RESERVED");
    }
}
