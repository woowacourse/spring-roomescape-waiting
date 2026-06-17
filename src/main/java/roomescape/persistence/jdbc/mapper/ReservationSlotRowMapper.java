package roomescape.persistence.jdbc.mapper;

import java.util.List;
import org.springframework.jdbc.core.RowMapper;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.TimeStatus;

public final class ReservationSlotRowMapper {

    public static final RowMapper<ReservationSlot> RESERVATION_SLOT_WITHOUT_RESERVATIONS_ROW_MAPPER = (rs, rowNum) -> {
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
                rs.getInt("price"),
                rs.getBoolean("is_active")
        );
        return new ReservationSlot(
                rs.getLong("res_id"),
                rs.getDate("res_date").toLocalDate(),
                theme,
                time,
                List.of()
        );
    };

    private ReservationSlotRowMapper() {
    }
}
