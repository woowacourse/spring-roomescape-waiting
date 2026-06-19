package roomescape.repository.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import org.springframework.jdbc.core.RowMapper;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationEntry;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.TimeStatus;

public final class ReservationEntityMapper {

    public static final RowMapper<ReservationEntry> RESERVATION_ENTRY_ROW_MAPPER = (rs, rowNum) ->
            ReservationEntry.restore(
                    rs.getLong("entry_id"),
                    rs.getString("entry_name"),
                    ReservationStatus.valueOf(rs.getString("entry_status")),
                    rs.getTimestamp("entry_created_at").toLocalDateTime()
            );

    private ReservationEntityMapper() {
    }

    public static Reservation mapReservation(ResultSet rs) throws SQLException {
        ReservationTime time = ReservationTime.restore(
                rs.getLong("time_id"),
                rs.getTime("time_start").toLocalTime(),
                TimeStatus.valueOf(rs.getString("time_status"))
        );
        Theme theme = Theme.restore(
                rs.getLong("theme_id"),
                rs.getString("theme_name"),
                rs.getString("description"),
                rs.getString("thumbnail_image_url"),
                rs.getLong("price"),
                rs.getBoolean("is_active")
        );
        return Reservation.restore(
                rs.getLong("res_id"),
                rs.getDate("res_date").toLocalDate(),
                theme,
                time,
                List.of()
        );
    }
}
