package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import roomescape.repository.JdbcReservationRepository;
import roomescape.service.dto.ReservationCreateCommand;
import roomescape.service.dto.ReservationUpdateCommand;

@SpringBootTest
class ReservationUpdateRollbackTest {

    private static final LocalDate DATE = LocalDate.of(2099, 12, 31);

    @Autowired
    private AdminReservationService reservationService;

    @Autowired
    private UserReservationService userReservationService;

    @MockitoSpyBean
    private JdbcReservationRepository reservationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("다른 슬롯으로 변경 후 원래 슬롯 승급이 실패하면 변경까지 함께 롤백된다")
    void 승급_실패_시_변경이_롤백된다() {
        long fromTimeId = insertTime("10:00");
        long toTimeId = insertTime("11:00");
        long themeId = insertTheme("롤백테마-변경승급");
        long ownerId = reservationService.create(
                new ReservationCreateCommand("owner", DATE, fromTimeId, themeId)).id();
        long waiterId = reservationService.create(
                new ReservationCreateCommand("waiter", DATE, fromTimeId, themeId)).id();

        doThrow(new RuntimeException("승급 실패 시뮬레이션"))
                .when(reservationRepository).promoteEarliestWaiting(any(LocalDate.class), anyLong(), anyLong());

        assertThatThrownBy(() -> userReservationService.update(
                new ReservationUpdateCommand(ownerId, "owner", DATE, toTimeId)))
                .isInstanceOf(RuntimeException.class);

        assertThat(timeIdOf(ownerId))
                .as("승급이 실패하면 같은 트랜잭션의 슬롯 이동도 롤백되어 원래 시간대를 유지해야 한다")
                .isEqualTo(fromTimeId);
        assertThat(statusOf(ownerId))
                .as("롤백되었으므로 원 예약은 확정 상태를 유지해야 한다")
                .isEqualTo("CONFIRMED");
        assertThat(statusOf(waiterId))
                .as("롤백되었으므로 대기자는 승급되지 않고 대기 상태를 유지해야 한다")
                .isEqualTo("WAITING");
    }

    private String statusOf(long id) {
        return jdbcTemplate.queryForObject(
                "SELECT status FROM reservation WHERE id = ?", String.class, id);
    }

    private long timeIdOf(long id) {
        return jdbcTemplate.queryForObject(
                "SELECT time_id FROM reservation WHERE id = ?", Long.class, id);
    }

    private long insertTime(String startAt) {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", startAt);
        return jdbcTemplate.queryForObject("SELECT MAX(id) FROM reservation_time", Long.class);
    }

    private long insertTheme(String name) {
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url) VALUES (?, '설명', 'http://x')", name);
        return jdbcTemplate.queryForObject("SELECT MAX(id) FROM theme", Long.class);
    }
}
