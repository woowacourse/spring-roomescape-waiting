package roomescape.dao.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import roomescape.domain.ReservationTime;

@Component
public class ReservationTimeRowMapper implements RowMapper<ReservationTime> {
    @Override
    public ReservationTime mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return new ReservationTime(
                resultSet.getLong("id"),
                LocalTime.parse(resultSet.getString("start_at"))
        );
    }

    public ReservationTime mapJoinedRow(ResultSet resultSet) throws SQLException {
        return new ReservationTime(
                resultSet.getLong("time_id"),
                LocalTime.parse(resultSet.getString("time_start_at"))
        );
    }
}
