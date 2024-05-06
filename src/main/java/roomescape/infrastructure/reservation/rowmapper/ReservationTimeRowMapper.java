package roomescape.infrastructure.reservation.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import roomescape.domain.reservation.ReservationTime;

public class ReservationTimeRowMapper {

    private ReservationTimeRowMapper() {
    }

    public static ReservationTime mapRow(ResultSet rs) throws SQLException {
        long id = rs.getLong("id");
        String startAt = rs.getString("start_at");
        return new ReservationTime(id, LocalTime.parse(startAt));
    }

    public static ReservationTime joinedMapRow(ResultSet rs) throws SQLException {
        long id = rs.getLong("time_id");
        String startAt = rs.getString("start_at");
        return new ReservationTime(id, LocalTime.parse(startAt));
    }
}
