package roomescape.repository.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import roomescape.domain.reservation.Reservation;

@Component
public class ReservationRowMapper implements RowMapper<Reservation> {

    @Override
    public Reservation mapRow(ResultSet resultSet, int rowNum) throws SQLException {
        return new Reservation(
                resultSet.getLong("reservation_id"),
                resultSet.getLong("member_id"),
                resultSet.getString("member_email"),
                resultSet.getString("member_password"),
                resultSet.getString("member_name"),
                resultSet.getString("member_role"),
                resultSet.getLong("theme_id"),
                resultSet.getString("theme_name"),
                resultSet.getString("theme_description"),
                resultSet.getString("theme_thumbnail"),
                resultSet.getString("reservation_date"),
                resultSet.getLong("time_id"),
                resultSet.getString("time_value")
        );
    }
}
