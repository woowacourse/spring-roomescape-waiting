package roomescape.integration.fixture;

import java.time.LocalTime;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Component;
import roomescape.domain.time.ReservationTime;

@Component
public class ReservationTimeDbFixture {
    private final SimpleJdbcInsert simpleJdbcInsert;

    public ReservationTimeDbFixture(final JdbcTemplate jdbcTemplate) {
        this.simpleJdbcInsert = new SimpleJdbcInsert(jdbcTemplate)
                .withTableName("reservation_time")
                .usingGeneratedKeyColumns("id");
    }

    public ReservationTime 예약시간_10시() {
        return 예약시간(LocalTime.of(10, 0));
    }

    public ReservationTime 예약시간_11시() {
        return 예약시간(LocalTime.of(11, 0));
    }

    public ReservationTime 예약시간(final LocalTime startAt) {
        Long id = simpleJdbcInsert.executeAndReturnKey(new MapSqlParameterSource()
                .addValue("start_at", startAt)).longValue();
        return new ReservationTime(id, startAt);
    }
}
