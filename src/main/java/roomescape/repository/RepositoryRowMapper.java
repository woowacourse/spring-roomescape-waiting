package roomescape.repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.reservation.ReservationName;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Slot;
import roomescape.domain.reservation.Status;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeName;
import roomescape.domain.theme.ThumbnailUrl;

public final class RepositoryRowMapper {
    private RepositoryRowMapper() {
    }

    public static Reservation reservationRowMapper(ResultSet rs) throws SQLException {
        return Reservation.load(
                rs.getLong("reservation_id"),
                new ReservationName(rs.getString("name")),
                slotRowMapper(rs),
                Status.valueOf(rs.getString("status")),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }

    public static Slot slotRowMapper(ResultSet rs) throws SQLException {
        return Slot.load(
                rs.getLong("slot_id"),
                new ReservationDate(rs.getDate("slot_date").toLocalDate()),
                reservationTimeRowMapper(rs),
                themeRowMapper(rs)
        );
    }

    public static ReservationTime reservationTimeRowMapper(ResultSet rs) throws SQLException {
        return ReservationTime.of(rs.getLong("time_id"), rs.getTime("start_at").toLocalTime());
    }

    public static Theme themeRowMapper(ResultSet rs) throws SQLException {
        return Theme.load(
                rs.getLong("theme_id"),
                new ThemeName(rs.getString("theme_name")),
                rs.getString("description"),
                new ThumbnailUrl(rs.getString("thumbnail_url"))
        );
    }
}
