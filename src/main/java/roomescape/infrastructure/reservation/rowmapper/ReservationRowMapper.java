package roomescape.infrastructure.reservation.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import roomescape.domain.reservation.Reservation;
import roomescape.infrastructure.member.rowmapper.MemberRowMapper;

public class ReservationRowMapper {

    private ReservationRowMapper() {
    }

    public static Reservation joinedMapRow(ResultSet rs) throws SQLException {
        long id = rs.getLong("reservation_id");
        String date = rs.getString("date");
        return new Reservation(
                id,
                MemberRowMapper.joinedMapRow(rs),
                LocalDate.parse(date),
                ReservationTimeRowMapper.joinedMapRow(rs),
                ThemeRowMapper.joinedMapRow(rs),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
