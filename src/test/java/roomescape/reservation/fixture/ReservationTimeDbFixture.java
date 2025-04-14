package roomescape.reservation.fixture;

import java.time.LocalTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import roomescape.time.domain.ReservationTime;

@Component
public class ReservationTimeDbFixture {

    private JdbcTemplate jdbcTemplate;

    public ReservationTimeDbFixture(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public ReservationTime 예약시간_10시() {
        SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");

        LocalTime startAt = LocalTime.of(10, 0);

        Long id = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource()
                .addValue("start_at", startAt)
        ).longValue();

        return new ReservationTime(id, startAt);
    }
}
