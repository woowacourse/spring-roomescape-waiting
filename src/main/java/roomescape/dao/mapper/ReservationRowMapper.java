package roomescape.dao.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.domain.user.Member;

@Component
public class ReservationRowMapper implements RowMapper<Reservation> {

    @Override
    public Reservation mapRow(final ResultSet rs, final int rowNum) throws SQLException {
        final long reservationId = rs.getLong("reservation_id");
        final String date = rs.getString("reservation_date");

        final long timeId = rs.getLong("time_id");
        final String startAt = rs.getString("time_value");

        final long themeId = rs.getLong("theme_id");
        final String themeName = rs.getString("theme_name");
        final String description = rs.getString("theme_description");
        final String thumbnail = rs.getString("theme_thumbnail");

        final long memberId = rs.getLong("member_id");
        final String memberName = rs.getString("member_name");
        final String email = rs.getString("member_email");
        final String password = rs.getString("member_password");
        final String role = rs.getString("member_role");

        return new Reservation(
                reservationId,
                ReservationDate.from(date),
                ReservationTime.from(timeId, startAt),
                Theme.of(themeId, themeName, description, thumbnail),
                Member.from(memberId, memberName, email, password,role)
        );
    }
}
