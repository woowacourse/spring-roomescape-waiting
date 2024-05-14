package roomescape.infrastructure.persistence.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import roomescape.domain.ReservationTime;

public class ReservationTimeRowMapper {

    private ReservationTimeRowMapper() {
    }

    public static ReservationTime mapRow(ResultSet rs, int rowNum) throws SQLException {
        long id = rs.getLong("reservation_time_id");
        String startAt = rs.getString("reservation_time_start_at");
        return new ReservationTime(id, LocalTime.parse(startAt));
    }
}
