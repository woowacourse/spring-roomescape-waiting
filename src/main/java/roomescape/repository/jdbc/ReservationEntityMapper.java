package roomescape.repository.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;
import roomescape.domain.ReservationSlot;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.TimeStatus;

public final class ReservationEntityMapper {

    public static final RowMapper<Reservation> RESERVATION_ROW_MAPPER = (rs, rowNum) ->
            new Reservation(
                    rs.getLong("reservation_id"),
                    rs.getString("reservation_name"),
                    null,
                    ReservationStatus.valueOf(rs.getString("reservation_status")),
                    rs.getTimestamp("reservation_created_at").toLocalDateTime()
            );

    public static ReservationSlot mapReservationSlot(ResultSet rs) throws SQLException {
        ReservationTime time = new ReservationTime(
                rs.getLong("time_id"),
                rs.getTime("time_start").toLocalTime(),
                TimeStatus.valueOf(rs.getString("time_status"))
        );
        Theme theme = new Theme(
                rs.getLong("theme_id"),
                rs.getString("theme_name"),
                rs.getString("description"),
                rs.getString("thumbnail_image_url"),
                rs.getBoolean("is_active")
        );
        return new ReservationSlot(
                rs.getLong("res_id"),
                rs.getDate("res_date").toLocalDate(),
                theme,
                time,
                List.of()
        );
    }

    private ReservationEntityMapper() {}
}
