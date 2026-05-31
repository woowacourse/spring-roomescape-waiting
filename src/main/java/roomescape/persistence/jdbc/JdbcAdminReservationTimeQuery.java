package roomescape.persistence.jdbc;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import roomescape.controller.admin.api.dto.response.AdminReservationTimeResponse;
import roomescape.controller.admin.api.query.AdminReservationTimeQuery;
import roomescape.persistence.jdbc.mapper.AdminReservationTimeResponseRowMapper;

@Component
@RequiredArgsConstructor
public class JdbcAdminReservationTimeQuery implements AdminReservationTimeQuery {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<AdminReservationTimeResponse> getAllReservationTimes() {
        String sql = "SELECT id, start_at, status FROM reservation_time ORDER BY id ASC";
        return jdbcTemplate.query(sql, AdminReservationTimeResponseRowMapper.ADMIN_RESERVATION_TIME_RESPONSE_ROW_MAPPER);
    }
}
