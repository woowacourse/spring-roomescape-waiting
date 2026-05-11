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
    void 예약_생성_테스트() {
        Reservation reservation = new Reservation("초록", 1L, LocalDate.parse("2026-05-05"),
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
        LocalDate date = LocalDate.parse("2026-05-05");
        List<Long> times = reservationDao.findTimeIdByThemeIdAndDate(themeId, date);

        assertThat(times.size()).isEqualTo(3);
    }

}
