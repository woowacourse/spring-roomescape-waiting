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
                            rs.getTime("start_at").toLocalTime())
            );

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ReservationDao reservationDao;

    @Test
    void 예약_생성_성공() {
        Reservation reservation = new Reservation("초록", 1L, LocalDate.now().plusDays(1),
                new ReservationTime(6L, LocalTime.parse("15:00")));
        Reservation expected = reservationDao.insert(reservation);

        String sql =
                "select r.id as reservation_id, r.name, r.date, t.id as time_id, t.start_at as start_at, r.theme_id as theme_id "
                        + "from reservation r "
                        + "inner join reservation_time t "
                        + "on r.time_id = t.id "
                        + "and r.id = ?";
        Reservation actual = jdbcTemplate.query(sql, rowMapper, expected.getId()).getFirst();

        assertThat(expected.getId()).isEqualTo(actual.getId());
        assertThat(expected.getName()).isEqualTo(actual.getName());
        assertThat(expected.getDate()).isEqualTo(actual.getDate());
        assertThat(expected.getTime().getId()).isEqualTo(actual.getTime().getId());
        assertThat(expected.getTime().getStartAt()).isEqualTo(actual.getTime().getStartAt());
        assertThat(expected.getThemeId()).isEqualTo(actual.getThemeId());
    }

    @Test
    void 예약_시간_조회_성공() {
        Long themeId = 2L;
        LocalDate date = LocalDate.now().plusDays(1);
        reservationDao.insert(new Reservation("초록", themeId, date, new ReservationTime(1L, LocalTime.parse("10:00"))));
        reservationDao.insert(new Reservation("브라운", themeId, date, new ReservationTime(2L, LocalTime.parse("11:00"))));
        reservationDao.insert(new Reservation("로치", themeId, date, new ReservationTime(3L, LocalTime.parse("12:00"))));

        List<Long> times = reservationDao.selectTimeIdByThemeIdAndDate(themeId, date);

        assertThat(times.size()).isEqualTo(3);
    }

    @Test
    void 이름으로_예약_조회_성공() {
        String name = "에버";
        LocalDate date = LocalDate.now().plusDays(2);
        Reservation first = reservationDao.insert(
                new Reservation(name, 1L, date, new ReservationTime(1L, LocalTime.parse("10:00")))
        );
        Reservation second = reservationDao.insert(
                new Reservation(name, 1L, date, new ReservationTime(2L, LocalTime.parse("11:00")))
        );
        reservationDao.insert(new Reservation("워넬", 1L, date, new ReservationTime(3L, LocalTime.parse("12:00"))));

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
        reservationDao.insert(new Reservation(name, themeId, date, reservationTime));

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
}
