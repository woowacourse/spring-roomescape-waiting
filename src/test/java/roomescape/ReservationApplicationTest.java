package roomescape;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import roomescape.exception.ErrorCode;
import roomescape.exception.ResourceNotFoundException;
import roomescape.reservation.service.ReservationService;
import roomescape.reservationwaiting.service.ReservationWaitingService;
/**
 * 예약 승격 테스트
 * 1. [성공] 대기가 존재하지 않는 예약 삭제 성공
 * 2. [성공] 대기가 존재하는 예약 삭제 성공
 * 3. [롤백] 대기 조회 성공 후 대기 삭제 실패 시 롤백
 * 4. [롤백] 대기 조회 성공 후 대기 승격 실패 시 롤백
 */
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ReservationApplicationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReservationApplicationService reservationApplicationService;

    @MockitoSpyBean
    private ReservationWaitingService reservationWaitingService;

    @MockitoSpyBean
    private ReservationService reservationService;

    @BeforeEach
    public void setup() {
        settingTables();
    }

    private static final LocalDate TOMORROW = LocalDate.now().plusDays(1);

    @Test
    void 대기가_존재하지_않는_예약_삭제_성공() {
        // given
        String name = "도우너";
        insertReservation(name, TOMORROW, 1L, 1L);
        Long reservationId = jdbcTemplate.queryForObject("select id from reservation where name = ? LIMIT 1", Long.class, name);

        // when
        reservationApplicationService.cancelReservation(reservationId, name);

        // then
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservation", Integer.class);
        assertThat(count).isEqualTo(0);
    }

    @Test
    void 대기가_존재하는_예약_삭제_시_예약_삭제_후_대기_승격_성공() {
        // given
        String name = "도우너";
        insertReservation("도우너", TOMORROW, 1L, 1L);
        insertReservationWaiting("둘리", TOMORROW, 1L, 1L, LocalDateTime.now());
        Long reservationId = jdbcTemplate.queryForObject("select id from reservation where name = ? LIMIT 1", Long.class, name);

        // when
        reservationApplicationService.cancelReservation(reservationId, name);

        // then
        Integer reservationCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservation", Integer.class);
        Integer waitingCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservation_waiting", Integer.class);
        String reservationName = jdbcTemplate.queryForObject("select name from reservation LIMIT 1", String.class);

        assertAll(
                () -> assertThat(reservationCount).isEqualTo(1),
                () -> assertThat(waitingCount).isEqualTo(0),
                () -> assertThat(reservationName).isEqualTo("둘리")
        );

    }

    @Test
    void 대기_삭제_실패시_예약_삭제도_롤백() {
        // given
        insertReservation("도우너", TOMORROW, 1L, 1L);
        insertReservationWaiting("둘리", TOMORROW, 1L, 1L, LocalDateTime.now());

        Long reservationId = jdbcTemplate.queryForObject("SELECT id FROM reservation WHERE name = ? LIMIT 1", Long.class, "도우너");

        doThrow(new ResourceNotFoundException(ErrorCode.RESERVATION_WAITING_NOT_FOUND,
                "강제로 오류 발생"))
                .when(reservationWaitingService).deleteById(anyLong());

        // when
        assertThrows(ResourceNotFoundException.class,
                () -> reservationApplicationService.cancelReservation(reservationId, "도우너"));

        // then
        Integer reservationCount = jdbcTemplate.queryForObject("SELECT count(*) FROM reservation", Integer.class);
        String remainingName = jdbcTemplate.queryForObject("SELECT name FROM reservation LIMIT 1", String.class);
        Integer waitingCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservation_waiting", Integer.class);

        assertAll(
                () -> assertThat(reservationCount).isEqualTo(1),
                () -> assertThat(remainingName).isEqualTo("도우너"),
                () -> assertThat(waitingCount).isEqualTo(1)
        );
    }

    @Test
    void 대기_승격_실패시_예약_삭제도_롤백() {
        // given
        insertReservation("도우너", TOMORROW, 1L, 1L);
        insertReservationWaiting("둘리", TOMORROW, 1L, 1L, LocalDateTime.now());

        Long reservationId = jdbcTemplate.queryForObject("SELECT id FROM reservation WHERE name = ? LIMIT 1", Long.class, "도우너");

        doThrow(new RuntimeException("승격 실패"))
                .when(reservationService).save(any(), any(), anyLong(), anyLong());

        // when
        assertThrows(RuntimeException.class,
                () -> reservationApplicationService.cancelReservation(reservationId, "도우너"));

        // then
        Integer count = jdbcTemplate.queryForObject("SELECT count(*) FROM reservation", Integer.class);
        String name = jdbcTemplate.queryForObject("select name from reservation LIMIT 1", String.class);
        Integer waitingCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservation_waiting", Integer.class);

        assertAll(
                () -> assertThat(count).isEqualTo(1),
                () -> assertThat(name).isEqualTo("도우너"),
                () -> assertThat(waitingCount).isEqualTo(1)
        );
    }

    private void insertReservation(final String name, final LocalDate date, final Long themeId, final Long timeId) {
        jdbcTemplate.update(
                "INSERT INTO reservation (name, date, theme_id, time_id) VALUES (?, ?, ?, ?)",
                name,
                java.sql.Date.valueOf(date),
                themeId,
                timeId
        );
    }

    private void insertReservationWaiting(final String name, final LocalDate date, final Long themeId, final Long timeId, final LocalDateTime requestedAt) {
        jdbcTemplate.update(
                "INSERT INTO reservation_waiting (name, date, theme_id, time_id, requested_at) VALUES (?, ?, ?, ?, ?)",
                name,
                date,
                themeId,
                timeId,
                requestedAt
        );
    }

    private void settingTables() {
        jdbcTemplate.update("DELETE FROM reservation_waiting");
        jdbcTemplate.update("DELETE FROM reservation");
        jdbcTemplate.update("DELETE FROM reservation_time");
        jdbcTemplate.update("DELETE FROM theme");
        jdbcTemplate.update("ALTER TABLE reservation_waiting ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE reservation_time ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("ALTER TABLE theme ALTER COLUMN id RESTART WITH 1");

        jdbcTemplate.update("INSERT INTO reservation_time (start_at) VALUES (?)", "10:00");
        jdbcTemplate.update("INSERT INTO theme (name, description, thumbnail_url) VALUES (?, ?, ?)", "어둠의 테마", "어둠의 테마 입니다", "http:kkk.jpg");
    }
}
