package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static roomescape.config.FixedClockConfig.FUTURE_DATE;
import static roomescape.config.FixedClockConfig.NOW_TIME;
import static roomescape.config.FixedClockConfig.TODAY;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ThemeDao;
import roomescape.dao.WaitingDao;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.UserName;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.reservation.theme.Theme;
import roomescape.domain.reservation.time.ReservationTime;
import roomescape.service.dto.command.ReservationCommand;

@SpringBootTest
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class ReservationServiceTransactionTest {

    private static final String RESERVATION_OWNER = "토리";
    private static final String WAITER_NAME = "로키";

    private final LocalDate date = LocalDate.parse(FUTURE_DATE);
    private final Long timeId = 5L;
    private final Long themeId = 1L;

    @Autowired
    private ReservationDao reservationDao;

    @MockitoSpyBean
    private WaitingDao waitingDao;

    @Autowired
    private ReservationTimeDao reservationTimeDao;

    @Autowired
    private ThemeDao themeDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReservationService reservationService;

    private Long reservationId;
    private Long waitingId;

    @BeforeEach
    void setUp() {
        jdbcTemplate.update("DELETE FROM waiting");
        jdbcTemplate.update("DELETE FROM reservation");

        ReservationTime time = reservationTimeDao.findTimeById(timeId).orElseThrow();
        Theme theme = themeDao.findThemeById(themeId).orElseThrow();

        Reservation reservation = reservationDao.save(new Reservation(
                UserName.parse(RESERVATION_OWNER), date, time, theme
        ));
        reservationId = reservation.getId();

        LocalDateTime createdAt = LocalDateTime.of(LocalDate.parse(TODAY), LocalTime.parse(NOW_TIME));
        Waiting waiting = waitingDao.save(new Waiting(
                UserName.parse(WAITER_NAME), date, time, theme, createdAt
        ));
        waitingId = waiting.getId();
    }

    @AfterEach
    void cleanup() {
        jdbcTemplate.update("DELETE FROM waiting");
        jdbcTemplate.update("DELETE FROM reservation");
    }

    @Test
    void 대기_승격_실패_시_예약_삭제도_롤백된다() {
        // given
        doThrow(new RuntimeException("승격의 마지막의 대기가 삭제 실패하는 경우")).when(waitingDao).delete(waitingId);

        // when
        assertThatThrownBy(() -> reservationService.cancelReservation(reservationId, RESERVATION_OWNER))
                .isInstanceOf(RuntimeException.class);

        // then
        assertThat(reservationDao.findById(reservationId)).isPresent();
        assertThat(waitingDao.findById(waitingId)).isPresent();
    }

    @Test
    void 대기_승격_실패_시_예약_변경도_롤백된다() {
        // given
        Long newTimeId = 6L;
        ReservationCommand command = new ReservationCommand(RESERVATION_OWNER, date, newTimeId, themeId);
        doThrow(new RuntimeException("승격의 마지막의 대기가 삭제 실패하는 경우")).when(waitingDao).delete(waitingId);

        // when
        assertThatThrownBy(() -> reservationService.changeReservationSlot(reservationId, command))
                .isInstanceOf(RuntimeException.class);

        // then
        Reservation rolledBack = reservationDao.findById(reservationId).orElseThrow();
        assertThat(rolledBack.getTime().getId()).isEqualTo(timeId);
        assertThat(waitingDao.findById(waitingId)).isPresent();
    }

    @Test
    void 대기_승격_실패_시_관리자_예약_삭제도_롤백된다() {
        // given
        doThrow(new RuntimeException("승격의 마지막의 대기가 삭제 실패하는 경우")).when(waitingDao).delete(waitingId);

        // when
        assertThatThrownBy(() -> reservationService.removeReservation(reservationId))
                .isInstanceOf(RuntimeException.class);

        // then
        assertThat(reservationDao.findById(reservationId)).isPresent();
        assertThat(waitingDao.findById(waitingId)).isPresent();
    }

}