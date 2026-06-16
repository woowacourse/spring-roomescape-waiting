package roomescape.reservation.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.reservation.Reservation;
import roomescape.reservation.ReservationStatus;
import roomescape.time.ReservationTime;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ReservationDaoTest {
    private static final RowMapper<Reservation> rowMapper = (rs, rowNum) ->
            new Reservation(
                    rs.getLong("reservation_id"),
                    rs.getString("name"),
                    rs.getLong("theme_id"),
                    rs.getDate("date").toLocalDate(),
                    new ReservationTime(rs.getLong("time_id"),
                            rs.getTime("start_at").toLocalTime()),
                    ReservationStatus.valueOf(rs.getString("status"))
            );

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReservationDao reservationDao;

    @Test
    void 예약_시간_조회_성공() {
        Long themeId = 2L;
        LocalDate date = LocalDate.now().plusDays(1);
        reservationDao.insert(new Reservation("초록", themeId, date, new ReservationTime(1L, LocalTime.parse("10:00")), ReservationStatus.CONFIRMED));
        reservationDao.insert(new Reservation("브라운", themeId, date, new ReservationTime(2L, LocalTime.parse("11:00")), ReservationStatus.CONFIRMED));
        reservationDao.insert(new Reservation("로치", themeId, date, new ReservationTime(3L, LocalTime.parse("12:00")), ReservationStatus.CONFIRMED));

        List<Long> times = reservationDao.selectTimeIdByThemeIdAndDate(themeId, date);

        assertThat(times.size()).isEqualTo(3);
    }

    @Test
    void 이름으로_예약_조회_성공() {
        String name = "에버";
        LocalDate date = LocalDate.now().plusDays(2);
        Reservation first = reservationDao.insert(
                new Reservation(name, 1L, date, new ReservationTime(1L, LocalTime.parse("10:00")), ReservationStatus.CONFIRMED)
        );
        Reservation second = reservationDao.insert(
                new Reservation(name, 1L, date, new ReservationTime(2L, LocalTime.parse("11:00")), ReservationStatus.CONFIRMED)
        );
        reservationDao.insert(new Reservation("워넬", 1L, date, new ReservationTime(3L, LocalTime.parse("12:00")), ReservationStatus.CONFIRMED));

        List<Reservation> reservations = reservationDao.selectByName(name);

        assertThat(reservations).hasSize(2)
                .extracting(Reservation::getId)
                .containsExactly(first.getId(), second.getId());
        assertThat(reservations)
                .extracting(Reservation::getName)
                .containsOnly(name);
    }

    @Test
    void 예약의_날짜와_시간_수정_성공() {
        Long reservationId = 1L;
        LocalDate changedDate = LocalDate.now().plusDays(1);
        Long changedTimeId = 2L;

        reservationDao.updateDateTimeById(reservationId, changedDate, changedTimeId);

        Reservation reservation = reservationDao.selectById(reservationId)
                .orElseThrow();

        assertThat(reservation.getDate()).isEqualTo(changedDate);
        assertThat(reservation.getTime().getId()).isEqualTo(changedTimeId);
    }

    @Test
    void 예약이_존재하는_경우_true_반환() {
        String name = "초록";
        Long themeId = 1L;
        LocalDate date = LocalDate.now().plusDays(1);
        ReservationTime reservationTime = new ReservationTime(6L, LocalTime.parse("15:00"));
        reservationDao.insert(new Reservation(name, themeId, date, reservationTime, ReservationStatus.CONFIRMED));

        boolean actual = reservationDao.existsByNameAndThemeIdAndDateAndTimeId(name, themeId, date, reservationTime.getId());

        assertThat(actual).isTrue();
    }

    @Test
    void 예약이_존재하지_않는_경우_false_반환() {
        String name = "초록";
        Long themeId = 1L;
        LocalDate date = LocalDate.now().plusDays(1);
        Long timeId = 6L;

        boolean actual = reservationDao.existsByNameAndThemeIdAndDateAndTimeId(name, themeId, date, timeId);

        assertThat(actual).isFalse();
    }

    @Test
    void 미래_날짜_예약이_있으면_upcoming_true() {
        Long timeId = 5L; // data.sql: 14:00, 시드 예약 없음
        reservationDao.insert(new Reservation("초록", 1L, LocalDate.now().plusDays(1),
                new ReservationTime(timeId, LocalTime.parse("14:00")), ReservationStatus.CONFIRMED));

        boolean actual = reservationDao.existsUpcomingByTimeId(timeId, LocalDate.now(), LocalTime.now());

        assertThat(actual).isTrue();
    }

    @Test
    void 과거_날짜_예약만_있으면_upcoming_false() {
        Long timeId = 5L;
        reservationDao.insert(new Reservation("초록", 1L, LocalDate.now().minusDays(1),
                new ReservationTime(timeId, LocalTime.parse("14:00")), ReservationStatus.CONFIRMED));

        boolean actual = reservationDao.existsUpcomingByTimeId(timeId, LocalDate.now(), LocalTime.now());

        assertThat(actual).isFalse();
    }

    @Test
    void 당일_아직_지나지_않은_시간이면_upcoming_true() {
        Long timeId = 5L; // 14:00
        reservationDao.insert(new Reservation("초록", 1L, LocalDate.now(),
                new ReservationTime(timeId, LocalTime.parse("14:00")), ReservationStatus.CONFIRMED));

        // now=13:00 기준 → 14:00 은 아직 안 지남
        boolean actual = reservationDao.existsUpcomingByTimeId(timeId, LocalDate.now(), LocalTime.of(13, 0));

        assertThat(actual).isTrue();
    }

    @Test
    void 당일_이미_지난_시간이면_upcoming_false() {
        Long timeId = 5L; // 14:00
        reservationDao.insert(new Reservation("초록", 1L, LocalDate.now(),
                new ReservationTime(timeId, LocalTime.parse("14:00")), ReservationStatus.CONFIRMED));

        // now=15:00 기준 → 14:00 은 이미 지남
        boolean actual = reservationDao.existsUpcomingByTimeId(timeId, LocalDate.now(), LocalTime.of(15, 0));

        assertThat(actual).isFalse();
    }
}
