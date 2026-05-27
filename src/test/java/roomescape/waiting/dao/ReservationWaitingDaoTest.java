package roomescape.waiting.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.waiting.ReservationWaiting;

@DataJdbcTest
@Import(ReservationWaitingDao.class)
class ReservationWaitingDaoTest {

    @Autowired
    private ReservationWaitingDao reservationWaitingDao;

    @Test
    void 존재하지_않는_예약_대기_조회_성공() {
        String name = "워넬";
        Long themeId = 1L;
        LocalDate date = LocalDate.now();
        Long timeId = 1L;

        Boolean actual = reservationWaitingDao.existsByNameAndDateAndThemeIdAndTimeId(name, themeId, date, timeId);

        assertThat(actual).isFalse();
    }

    @Test
    void 존재하는_예약_대기_조회_성공() {
        String name = "워넬";
        Long themeId = 1L;
        LocalDate date = LocalDate.now();
        Long timeId = 1L;
        Long waitingNumber = 1L;

        ReservationWaiting reservationWaiting = new ReservationWaiting(name, themeId, date, timeId, waitingNumber);
        reservationWaitingDao.insert(reservationWaiting);
        Boolean actual = reservationWaitingDao.existsByNameAndDateAndThemeIdAndTimeId(name, themeId, date, timeId);

        assertThat(actual).isTrue();
    }

    @Test
    void 예약_대기_조회_성공() {
        ReservationWaiting reservationWaiting = new ReservationWaiting(
                "티버",
                1L,
                LocalDate.now(),
                1L,
                2L
        );

        ReservationWaiting expected =  reservationWaitingDao.insert(reservationWaiting);
        ReservationWaiting actual = reservationWaitingDao.selectById(expected.getId());

        assertThat(actual.getName()).isEqualTo(expected.getName());
        assertThat(actual.getThemeId()).isEqualTo(expected.getThemeId());
        assertThat(actual.getDate()).isEqualTo(expected.getDate());
        assertThat(actual.getTimeId()).isEqualTo(expected.getTimeId());
        assertThat(actual.getWaitingNumber()).isEqualTo(expected.getWaitingNumber());
    }

    @Test
    void 예약_대기_생성_성공() {
        ReservationWaiting reservationWaiting = new ReservationWaiting(
                "티버",
                1L,
                LocalDate.now(),
                1L,
                2L
        );

        ReservationWaiting expected = reservationWaitingDao.insert(reservationWaiting);

        ReservationWaiting actual = reservationWaitingDao.selectById(expected.getId());

        assertThat(actual.getName()).isEqualTo(expected.getName());
        assertThat(actual.getThemeId()).isEqualTo(expected.getThemeId());
        assertThat(actual.getDate()).isEqualTo(expected.getDate());
        assertThat(actual.getTimeId()).isEqualTo(expected.getTimeId());
        assertThat(actual.getWaitingNumber()).isEqualTo(expected.getWaitingNumber());
    }

    @Test
    void 예약_대기가_없을_때_순번_1_반환_성공() {
        Long expected = 1L;
        Long actual = reservationWaitingDao.findNextWaitingNumber(
                1L,
                LocalDate.now(),
                3L
        );

        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void 다음_순번_번호_찾기_성공() {
        ReservationWaiting reservationWaiting = new ReservationWaiting(
                "티버",
                1L,
                LocalDate.now(),
                1L,
                1L
        );
        reservationWaitingDao.insert(reservationWaiting);

        Long expected = 2L;
        Long actual = reservationWaitingDao.findNextWaitingNumber(
                1L,
                LocalDate.now(),
                1L
        );

        assertThat(actual).isEqualTo(expected);
    }
}