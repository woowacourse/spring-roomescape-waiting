package roomescape.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import roomescape.domain.reservation.ReservationTime;

@Component
public class ReservationTimeRowMapper implements RowMapper<ReservationTime> {

    @Override
    public ReservationTime mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return new ReservationTime(
                resultSet.getLong("id"),
                resultSet.getString("start_at")
        );
    }
}
