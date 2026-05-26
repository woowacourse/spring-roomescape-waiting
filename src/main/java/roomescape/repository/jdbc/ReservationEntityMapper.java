package roomescape.repository.jdbc;

import org.springframework.jdbc.core.RowMapper;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.TimeStatus;

public final class ReservationEntityMapper {

    public static final RowMapper<Reservation> RESERVATION_ROW_MAPPER = (rs, rowNum) -> {
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
        return new Reservation(
                rs.getLong("res_id"),
                rs.getString("res_name"),
                rs.getDate("res_date").toLocalDate(),
                theme,
                time
        );
    };

    private ReservationEntityMapper() {}
}
