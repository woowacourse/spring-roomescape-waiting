package roomescape;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import roomescape.domain.Reservation;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationWaitingRepository;
import roomescape.service.ReservationService;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
class ReservationTransactionTest {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 6, 5, 12, 0);
    private static final LocalDate DATE = LocalDate.of(2099, 1, 1);
    private static final LocalDate UPDATE_DATE = LocalDate.of(2099, 1, 2);

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @MockitoSpyBean
    private ReservationRepository reservationRepository;

    @MockitoSpyBean
    private ReservationWaitingRepository reservationWaitingRepository;

    @BeforeEach
    void setup() {
        jdbcTemplate.update("DELETE FROM reservation_waiting;");
        jdbcTemplate.update("DELETE FROM reservation;");
        jdbcTemplate.update("ALTER TABLE reservation_waiting ALTER COLUMN id RESTART WITH 1;");
        jdbcTemplate.update("ALTER TABLE reservation ALTER COLUMN id RESTART WITH 1;");
    }

    @Test
    void 예약_삭제_자동_승격_중_예약_등록에_실패하면_전체_작업이_롤백된다() {
        // given
        insertReservation(1L, "브라운", DATE, 1L, 1L);
        insertWaiting(1L, "구구", DATE, 1L, 1L);
        failPromotedReservationInsert("구구");

        // when & then
        assertThatThrownBy(() -> reservationService.deleteByAdmin(1L, NOW))
                .isInstanceOf(QueryTimeoutException.class);

        assertAll(
                () -> assertThat(countReservation("브라운", DATE, 1L, 1L)).isEqualTo(1),
                () -> assertThat(countReservation("구구", DATE, 1L, 1L)).isZero(),
                () -> assertThat(countWaiting("구구", DATE, 1L, 1L)).isEqualTo(1)
        );
    }

    @Test
    void 예약_변경_자동_승격_중_예약_등록에_실패하면_전체_작업이_롤백된다() {
        // given
        insertReservation(1L, "브라운", DATE, 1L, 1L);
        insertWaiting(1L, "구구", DATE, 1L, 1L);
        failPromotedReservationInsert("구구");

        // when & then
        assertThatThrownBy(() -> reservationService.updateByUser(1L, "브라운", UPDATE_DATE, 2L, NOW))
                .isInstanceOf(QueryTimeoutException.class);

        assertAll(
                () -> assertThat(countReservation("브라운", DATE, 1L, 1L)).isEqualTo(1),
                () -> assertThat(countReservation("브라운", UPDATE_DATE, 2L, 1L)).isZero(),
                () -> assertThat(countReservation("구구", DATE, 1L, 1L)).isZero(),
                () -> assertThat(countWaiting("구구", DATE, 1L, 1L)).isEqualTo(1)
        );
    }

    private void failPromotedReservationInsert(String name) {
        doThrow(new QueryTimeoutException("insert failed"))
                .when(reservationRepository)
                .insert(argThat(reservation -> hasName(reservation, name)));
    }

    private boolean hasName(Reservation reservation, String name) {
        return reservation != null && reservation.getName().equals(name);
    }

    private void insertReservation(Long id, String name, LocalDate date, Long timeId, Long themeId) {
        jdbcTemplate.update(
                "INSERT INTO reservation(id, name, date, time_id, theme_id) VALUES (?, ?, ?, ?, ?);",
                id,
                name,
                date,
                timeId,
                themeId
        );
    }

    private void insertWaiting(Long id, String name, LocalDate date, Long timeId, Long themeId) {
        jdbcTemplate.update(
                "INSERT INTO reservation_waiting(id, name, date, time_id, theme_id) VALUES (?, ?, ?, ?, ?);",
                id,
                name,
                date,
                timeId,
                themeId
        );
    }

    private int countReservation(String name, LocalDate date, Long timeId, Long themeId) {
        Integer count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM reservation
                        WHERE name = ?
                          AND date = ?
                          AND time_id = ?
                          AND theme_id = ?;
                        """,
                Integer.class,
                name,
                date,
                timeId,
                themeId
        );
        return count;
    }

    private int countWaiting(String name, LocalDate date, Long timeId, Long themeId) {
        Integer count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(*)
                        FROM reservation_waiting
                        WHERE name = ?
                          AND date = ?
                          AND time_id = ?
                          AND theme_id = ?;
                        """,
                Integer.class,
                name,
                date,
                timeId,
                themeId
        );
        return count;
    }
}
