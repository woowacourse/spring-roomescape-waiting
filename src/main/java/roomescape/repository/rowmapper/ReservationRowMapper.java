package roomescape.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Role;
import roomescape.domain.Theme;

@Component
public class ReservationRowMapper implements RowMapper<Reservation> {
    @Override
    public Reservation mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Reservation(
                rs.getLong("reservation_id"),
                new Member(
                        rs.getLong("member_id"),
                        rs.getString("member_name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        Role.valueOf(rs.getString("role"))
                ),
                rs.getDate("reservation_date").toLocalDate(),
                new ReservationTime(
                        rs.getLong("time_id"),
                        rs.getTime("time_value").toLocalTime()
                ),
                new Theme(
                        rs.getLong("theme_id"),
                        rs.getString("theme_name"),
                        rs.getString("description"),
                        rs.getString("thumbnail")
                )
        );
    }
}
