package roomescape.waiting.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
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

        ReservationWaiting reservationWaiting = new ReservationWaiting(name, themeId, date, reservationTime);
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
                new ReservationTime(1L, LocalTime.now().plusHours(1))
        );

        ReservationWaiting expected =  reservationWaitingDao.insert(reservationWaiting);
        ReservationWaiting actual = reservationWaitingDao.selectById(expected.getId()).get();

        assertThat(actual.getName()).isEqualTo(expected.getName());
        assertThat(actual.getThemeId()).isEqualTo(expected.getThemeId());
        assertThat(actual.getDate()).isEqualTo(expected.getDate());
        assertThat(actual.getTime().getId()).isEqualTo(expected.getTime().getId());
        assertThat(actual.getWaitingNumber()).isEqualTo(expected.getWaitingNumber());
    }

    @Test
    void 이름으로_예약_대기_목록_조회_성공() {
        LocalDate date = LocalDate.now();
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.now().plusHours(1));
        ReservationWaiting first = new ReservationWaiting("티버", 1L, date, reservationTime);
        ReservationWaiting second = new ReservationWaiting("티버", 2L, date, reservationTime);
        ReservationWaiting other = new ReservationWaiting("로치", 1L, date, reservationTime);
        reservationWaitingDao.insert(first);
        reservationWaitingDao.insert(second);
        reservationWaitingDao.insert(other);

        List<ReservationWaiting> actual = reservationWaitingDao.selectByName("티버");

        assertThat(actual).hasSize(2)
                .extracting(ReservationWaiting::getName)
                .containsOnly("티버");
        assertThat(actual)
                .extracting(ReservationWaiting::getWaitingNumber)
                .containsExactly(1L, 1L);
    }

    @Test
    void 예약_대기_삭제_성공() {
        ReservationWaiting reservationWaiting = new ReservationWaiting(
                "티버",
                1L,
                LocalDate.now(),
                new ReservationTime(1L, LocalTime.now().plusHours(1))
        );

        ReservationWaiting inserted = reservationWaitingDao.insert(reservationWaiting);
        reservationWaitingDao.deleteById(inserted.getId());

        Optional<ReservationWaiting> actual = reservationWaitingDao.selectById(inserted.getId());

        assertThat(actual).isEmpty();
    }

    @Test
    void 대기_취소_시_대기_번호_자동_재정렬_성공() {
        LocalDate date = LocalDate.now();
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.now().plusHours(1));
        ReservationWaiting first = reservationWaitingDao.insert(new ReservationWaiting("티버", 1L, date, reservationTime));
        reservationWaitingDao.insert(new ReservationWaiting("로치", 1L, date, reservationTime));
        reservationWaitingDao.insert(new ReservationWaiting("워넬", 1L, date, reservationTime));

        // 1번 대기자 취소
        reservationWaitingDao.deleteById(first.getId());

        List<ReservationWaiting> actual = reservationWaitingDao.selectByName("로치");

        // 로치가 자동으로 1번이 되는지 검증
        assertThat(actual.get(0).getWaitingNumber()).isEqualTo(1L);
    }
}
