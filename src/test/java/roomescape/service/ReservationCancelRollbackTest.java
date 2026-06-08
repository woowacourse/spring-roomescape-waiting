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

@SpringBootTest
class ReservationCancelRollbackTest {

    private static final LocalDate DATE = LocalDate.of(2099, 12, 31);

    @Autowired
    private AdminReservationService reservationService;

    @MockitoSpyBean
    private JdbcReservationRepository reservationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("취소 후 승급 처리가 실패하면 취소까지 함께 롤백된다")
    void 승급_실패_시_취소가_롤백된다() {
        long timeId = insertTime();
        long themeId = insertTheme("롤백테마-취소승급");
        long confirmedId = reservationService.create(
                new ReservationCreateCommand("owner", DATE, timeId, themeId)).id();
        long waiterId = reservationService.create(
                new ReservationCreateCommand("waiter", DATE, timeId, themeId)).id();

        doThrow(new RuntimeException("승급 실패 시뮬레이션"))
                .when(reservationRepository).promoteEarliestWaiting(any(LocalDate.class), anyLong(), anyLong());

        assertThatThrownBy(() -> reservationService.cancel(confirmedId))
                .isInstanceOf(RuntimeException.class);

        assertThat(statusOf(confirmedId))
                .as("승급이 실패하면 같은 트랜잭션의 취소도 롤백되어 확정 상태가 유지되어야 한다")
                .isEqualTo("CONFIRMED");
        assertThat(statusOf(waiterId))
                .as("롤백되었으므로 대기자는 승급되지 않고 대기 상태를 유지해야 한다")
                .isEqualTo("WAITING");
    }

    private String statusOf(long id) {
        return jdbcTemplate.queryForObject(
                "SELECT status FROM reservation WHERE id = ?", String.class, id);
    }

    private long insertTime() {
        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        return jdbcTemplate.queryForObject("SELECT MAX(id) FROM reservation_time", Long.class);
    }

    private long insertTheme(String name) {
        jdbcTemplate.update(
                "INSERT INTO theme (name, description, thumbnail_url) VALUES (?, '설명', 'http://x')", name);
        return jdbcTemplate.queryForObject("SELECT MAX(id) FROM theme", Long.class);
    }
}
