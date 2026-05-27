package roomescape.waiting.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;
import roomescape.time.ReservationTime;
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
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.now().plusHours(1));
        Long waitingNumber = 1L;

        ReservationWaiting reservationWaiting = new ReservationWaiting(name, themeId, date, reservationTime, waitingNumber);
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
                new ReservationTime(1L, LocalTime.now().plusHours(1)),
                2L
        );

        ReservationWaiting expected =  reservationWaitingDao.insert(reservationWaiting);
        ReservationWaiting actual = reservationWaitingDao.selectById(expected.getId()).get();

        assertThat(actual.getName()).isEqualTo(expected.getName());
        assertThat(actual.getThemeId()).isEqualTo(expected.getThemeId());
        assertThat(actual.getDate()).isEqualTo(expected.getDate());
        assertThat(actual.getReservationTime().getId()).isEqualTo(expected.getReservationTime().getId());
        assertThat(actual.getWaitingNumber()).isEqualTo(expected.getWaitingNumber());
    }

    @Test
    void 예약_대기_생성_성공() {
        ReservationWaiting reservationWaiting = new ReservationWaiting(
                "티버",
                1L,
                LocalDate.now(),
                new ReservationTime(1L, LocalTime.now().plusHours(1)),
                2L
        );

        ReservationWaiting expected = reservationWaitingDao.insert(reservationWaiting);

        ReservationWaiting actual = reservationWaitingDao.selectById(expected.getId()).get();

        assertThat(actual.getName()).isEqualTo(expected.getName());
        assertThat(actual.getThemeId()).isEqualTo(expected.getThemeId());
        assertThat(actual.getDate()).isEqualTo(expected.getDate());
        assertThat(actual.getReservationTime().getId()).isEqualTo(expected.getReservationTime().getId());
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
                new ReservationTime(1L, LocalTime.now().plusHours(1)),
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

    @Test
    void 예약_대기_삭제_성공() {
        ReservationWaiting reservationWaiting = new ReservationWaiting(
                "티버",
                1L,
                LocalDate.now(),
                new ReservationTime(1L, LocalTime.now().plusHours(1)),
                1L
        );

        ReservationWaiting inserted = reservationWaitingDao.insert(reservationWaiting);
        reservationWaitingDao.deleteById(inserted.getId());

        Optional<ReservationWaiting> actual = reservationWaitingDao.selectById(inserted.getId());

        assertThat(actual).isEmpty();
    }
}