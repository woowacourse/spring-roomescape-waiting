package roomescape.infrastructure.persistence.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import roomescape.domain.Reservation;

public class ReservationRowMapper {

    private ReservationRowMapper() {
    }

    public static Reservation joinedMapRow(ResultSet rs, int rowNum) throws SQLException {
        long id = rs.getLong("reservation_id");
        String date = rs.getString("reservation_date");
        return new Reservation(
                id,
                MemberRowMapper.mapRow(rs, rowNum),
                LocalDate.parse(date),
                ReservationTimeRowMapper.mapRow(rs, rowNum),
                ThemeRowMapper.mapRow(rs, rowNum)
        );
    }
}
