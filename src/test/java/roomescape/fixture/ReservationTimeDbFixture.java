package roomescape.fixture;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import roomescape.domain.ReservationTime;

import java.time.LocalTime;

@Component
public class ReservationTimeDbFixture {

    private final SimpleJdbcInsert jdbcInsert;

    public ReservationTimeDbFixture(JdbcTemplate jdbcTemplate) {
        this.jdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");

    }

    public ReservationTime 예약시간_10시() {
        LocalTime startAt = LocalTime.of(10, 0);

        Long id = jdbcInsert.executeAndReturnKey(new MapSqlParameterSource()
                .addValue("start_at", startAt)
        ).longValue();

        return ReservationTime.create(id, startAt);
    }
}
