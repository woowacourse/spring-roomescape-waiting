package roomescape.time.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import roomescape.time.ReservationTime;

@DataJdbcTest
@Import(TimeDao.class)
public class TimeDaoTest {
    private static final RowMapper<ReservationTime> timeRowMapper = (rs, rowNum) ->
            new ReservationTime(rs.getLong("id")
                    , rs.getTime("start_at").toLocalTime());

    @Autowired
    private TimeDao timeDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void 시간_생성_성공() {
        ReservationTime time = new ReservationTime(LocalTime.parse("21:00"));
        ReservationTime expected = timeDao.insert(time);

        String sql = "select id, start_at from reservation_time where id = ?";
        ReservationTime actual = jdbcTemplate.query(sql, timeRowMapper, expected.getId()).getFirst();

        assertThat(expected.getId()).isEqualTo(actual.getId());
        assertThat(expected.getStartAt()).isEqualTo(actual.getStartAt());
    }
}
