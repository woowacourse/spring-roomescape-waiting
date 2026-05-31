package roomescape.dao.rowMapper;

import org.springframework.jdbc.core.RowMapper;
import roomescape.domain.reservation.UserName;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.theme.Description;
import roomescape.domain.reservation.theme.ThemeName;
import roomescape.domain.reservation.theme.ThumbnailUrl;
import roomescape.domain.reservation.time.ReservationTime;
import roomescape.domain.reservation.theme.Theme;

public final class ReservationMapper {

    public static final RowMapper<Reservation> RESERVATION_ROW_MAPPER = (rs, rowNum) -> {
        ReservationTime time = new ReservationTime(
                rs.getLong("time_id"),
                rs.getTime("start_at").toLocalTime()
        );
        Theme theme = new Theme(
                rs.getLong("theme_id"),
                ThemeName.parse(rs.getString("theme_name")),
                Description.parse(rs.getString("description")),
                ThumbnailUrl.parse(rs.getString("url"))
        );
        return new Reservation(
                rs.getLong("id"),
                UserName.parse(rs.getString("name")),
                rs.getDate("date").toLocalDate(),
                time,
                theme
        );
    };

    private ReservationMapper() {
    }
}
