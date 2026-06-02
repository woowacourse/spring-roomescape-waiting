package roomescape.waiting.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
        LocalDateTime createdAt = LocalDateTime.now();

        ReservationWaiting reservationWaiting = new ReservationWaiting(name, themeId, date, reservationTime, createdAt);
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
                LocalDateTime.now()
        );

        ReservationWaiting expected = reservationWaitingDao.insert(reservationWaiting);
        ReservationWaiting actual = reservationWaitingDao.selectById(expected.getId()).get();

        assertThat(actual.getName()).isEqualTo(expected.getName());
        assertThat(actual.getThemeId()).isEqualTo(expected.getThemeId());
        assertThat(actual.getDate()).isEqualTo(expected.getDate());
        assertThat(actual.getTime().getId()).isEqualTo(expected.getTime().getId());
        assertThat(actual.getWaitingNumber()).isEqualTo(1L);
    }

    @Test
    void 이름으로_예약_대기_목록_조회_성공() {
        LocalDate date = LocalDate.now();
        ReservationTime firstTime = new ReservationTime(1L, LocalTime.parse("10:00"));
        ReservationTime secondTime = new ReservationTime(2L, LocalTime.parse("11:00"));
        LocalDateTime createdAt = LocalDateTime.now();
        ReservationWaiting first = new ReservationWaiting("티버", 1L, date, firstTime, createdAt);
        ReservationWaiting other = new ReservationWaiting("로치", 1L, date, secondTime, createdAt.plusSeconds(1));
        ReservationWaiting second = new ReservationWaiting("티버", 1L, date, secondTime, createdAt.plusSeconds(2));
        reservationWaitingDao.insert(first);
        reservationWaitingDao.insert(other);
        reservationWaitingDao.insert(second);

        List<ReservationWaiting> actual = reservationWaitingDao.selectByName("티버");

        assertThat(actual).hasSize(2)
                .extracting(ReservationWaiting::getName)
                .containsOnly("티버");
        assertThat(actual)
                .extracting(ReservationWaiting::getWaitingNumber)
                .containsExactly(1L, 2L);
    }

    @Test
    void 같은_생성_시간이면_id_순서로_예약_대기_목록을_조회_성공() {
        String name = "티버";
        LocalDate date = LocalDate.now().plusDays(1);
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        ReservationWaiting first = new ReservationWaiting(
                name,
                1L,
                date,
                new ReservationTime(1L, LocalTime.parse("10:00")),
                createdAt
        );
        ReservationWaiting second = new ReservationWaiting(
                name,
                1L,
                date,
                new ReservationTime(2L, LocalTime.parse("11:00")),
                createdAt
        );
        ReservationWaiting insertedFirst = reservationWaitingDao.insert(first);
        ReservationWaiting insertedSecond = reservationWaitingDao.insert(second);

        List<ReservationWaiting> actual = reservationWaitingDao.selectByName(name);

        assertThat(actual)
                .extracting(ReservationWaiting::getId)
                .containsExactly(insertedFirst.getId(), insertedSecond.getId());
    }

    @Test
    void 같은_생성_시간이면_id_순서로_예약_대기_순번을_계산_성공() {
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.parse("10:00"));
        LocalDateTime createdAt = LocalDateTime.of(2026, 1, 1, 10, 0);
        ReservationWaiting first = new ReservationWaiting("티버", 1L, date, reservationTime, createdAt);
        ReservationWaiting second = new ReservationWaiting("로치", 1L, date, reservationTime, createdAt);
        ReservationWaiting insertedFirst = reservationWaitingDao.insert(first);
        ReservationWaiting insertedSecond = reservationWaitingDao.insert(second);

        ReservationWaiting actualFirst = reservationWaitingDao.selectById(insertedFirst.getId()).get();
        ReservationWaiting actualSecond = reservationWaitingDao.selectById(insertedSecond.getId()).get();

        assertThat(actualFirst.getWaitingNumber()).isEqualTo(1L);
        assertThat(actualSecond.getWaitingNumber()).isEqualTo(2L);
    }

    @Test
    void 예약_대기_생성_성공() {
        ReservationWaiting reservationWaiting = new ReservationWaiting(
                "티버",
                1L,
                LocalDate.now(),
                new ReservationTime(1L, LocalTime.now().plusHours(1)),
                LocalDateTime.now()
        );

        ReservationWaiting expected = reservationWaitingDao.insert(reservationWaiting);

        ReservationWaiting actual = reservationWaitingDao.selectById(expected.getId()).get();

        assertThat(actual.getName()).isEqualTo(expected.getName());
        assertThat(actual.getThemeId()).isEqualTo(expected.getThemeId());
        assertThat(actual.getDate()).isEqualTo(expected.getDate());
        assertThat(actual.getTime().getId()).isEqualTo(expected.getTime().getId());
        assertThat(actual.getWaitingNumber()).isEqualTo(1L);
    }

    @Test
    void 예약_대기_삭제_성공() {
        ReservationWaiting reservationWaiting = new ReservationWaiting(
                "티버",
                1L,
                LocalDate.now(),
                new ReservationTime(1L, LocalTime.now().plusHours(1)),
                LocalDateTime.now()
        );

        ReservationWaiting inserted = reservationWaitingDao.insert(reservationWaiting);
        reservationWaitingDao.deleteById(inserted.getId());

        Optional<ReservationWaiting> actual = reservationWaitingDao.selectById(inserted.getId());

        assertThat(actual).isEmpty();
    }
}
