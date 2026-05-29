package roomescape.controller.admin.api.query;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import roomescape.controller.admin.api.dto.response.AdminReservationTimeResponse;

@Component
@RequiredArgsConstructor
public class AdminReservationTimeQuery {

    private static final RowMapper<AdminReservationTimeResponse> RESERVATION_TIME_RESPONSE_MAPPER = (rs, rowNum) ->
            new AdminReservationTimeResponse(
                    rs.getLong("id"),
                    rs.getTime("start_at").toLocalTime(),
                    rs.getString("status")
            );

    private final JdbcTemplate jdbcTemplate;

    public List<AdminReservationTimeResponse> getAllReservationTimes() {
        String sql = "SELECT id, start_at, status FROM reservation_time ORDER BY id ASC";
        return jdbcTemplate.query(sql, RESERVATION_TIME_RESPONSE_MAPPER);
    }
}
