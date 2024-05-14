package roomescape.dao.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import roomescape.dao.dto.AvailableReservationTimeResult;


@Component
public class AvailableReservationTimeMapper implements RowMapper<AvailableReservationTimeResult> {

    @Override
    public AvailableReservationTimeResult mapRow(final ResultSet rs, final int rowNum) throws SQLException {

        return new AvailableReservationTimeResult(
                rs.getBoolean("booked"),
                rs.getLong("time_id"),
                rs.getString("start_at")
        );
    }
}
