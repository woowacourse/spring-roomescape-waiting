package roomescape.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import roomescape.domain.ReservationTime;

@Component
public class ReservationTimeRowMapper implements RowMapper<ReservationTime> {
    @Override
    public ReservationTime mapRow(ResultSet rs, int rowNum) throws SQLException {
        Long id = rs.getLong("id");
        LocalTime time = rs.getTime("start_at").toLocalTime();
        return new ReservationTime(id, time);
    }
}
