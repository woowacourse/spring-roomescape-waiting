package roomescape.query;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import roomescape.service.result.ReservationTimeResult;

@Repository
@RequiredArgsConstructor
public class ReservationTimeQueryRepository {

    private static final RowMapper<ReservationTimeResult> ROW_MAPPER = (rs, rowNum) ->
            new ReservationTimeResult(
                    rs.getLong("id"),
                    rs.getTime("start_at").toLocalTime(),
                    rs.getString("status")
            );

    private final JdbcTemplate jdbcTemplate;

    public List<ReservationTimeResult> getAllReservationTimes() {
        String sql = "SELECT id, start_at, status FROM reservation_time";
        return jdbcTemplate.query(sql, ROW_MAPPER);
    }
}
