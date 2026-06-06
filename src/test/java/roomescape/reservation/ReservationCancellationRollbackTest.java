package roomescape.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.repository.reservationwaiting.ReservationWaitingRepository;
import roomescape.service.reservation.ReservationService;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:cancel-rollback")
class ReservationCancellationRollbackTest {

    @Autowired
    private ReservationService reservationService;

    @SpyBean
    private ReservationWaitingRepository reservationWaitingRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setup() {
        clearTables();
        createTheme(1L);
        createReservationTime(1L, "10:00:00");
        createReservation(1L, "쿠다", 1L, 1L);
        createReservationWaiting(1L, 1L, "아루", "2026-08-06 12:00:00");
    }

    @Test
    @DisplayName("전환 중 대기 삭제가 실패하면 예약 소유자 변경도 함께 롤백된다")
    void rollbackWhenWaitingDeletionFails() {
        // 승격의 두 번째 쓰기(대기 삭제)가 실패하도록 강제한다.
        doThrow(new RuntimeException("대기 삭제 실패"))
                .when(reservationWaitingRepository).deleteById(anyLong());

        assertThatThrownBy(() -> reservationService.deleteById(1L))
                .isInstanceOf(RuntimeException.class);

        // 첫 번째 쓰기(예약 소유자 변경)가 커밋되지 않고 롤백되어, 데이터가 원상태로 유지된다.
        String owner = jdbcTemplate.queryForObject(
                "SELECT name FROM reservation WHERE id = 1", String.class);
        Integer aruWaitingCount = jdbcTemplate.queryForObject(
                "SELECT count(1) FROM reservation_waiting WHERE reservation_id = 1 AND name = '아루'",
                Integer.class);

        assertThat(owner).isEqualTo("쿠다");
        assertThat(aruWaitingCount).isNotNull().isOne();
    }

    private void clearTables() {
        jdbcTemplate.update("DELETE FROM reservation_waiting");
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("DELETE FROM theme");
        jdbcTemplate.update("ALTER TABLE reservation_waiting ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE theme ALTER COLUMN id RESTART WITH 1");
    }

    private void createTheme(final long id) {
        jdbcTemplate.update(
                "INSERT INTO theme (id, name, description, thumbnail_url) VALUES (?, ?, ?, ?)",
                id, "미술관의 밤", "추리 테마", "https://example.com/theme.png");
    }

    private void createReservationTime(final long id, final String startAt) {
        jdbcTemplate.update("INSERT INTO reservation_time (id, start_at) VALUES (?, ?)", id, startAt);
    }

    private void createReservation(final long id, final String name, final long themeId, final long timeId) {
        jdbcTemplate.update(
                "INSERT INTO reservation (id, name, date, theme_id, time_id) VALUES (?, ?, ?, ?, ?)",
                id, name, "2026-08-06", themeId, timeId);
    }

    private void createReservationWaiting(
            final long id,
            final long reservationId,
            final String name,
            final String requestedAt
    ) {
        jdbcTemplate.update(
                "INSERT INTO reservation_waiting (id, reservation_id, name, requested_at) VALUES (?, ?, ?, ?)",
                id, reservationId, name, requestedAt);
    }
}
