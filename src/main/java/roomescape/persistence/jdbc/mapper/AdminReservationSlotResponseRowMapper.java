package roomescape.persistence.jdbc.mapper;

import org.springframework.jdbc.core.RowMapper;
import roomescape.controller.admin.api.dto.response.AdminReservationResponse;
import roomescape.controller.admin.api.dto.response.AdminReservationSlotResponse;
import roomescape.controller.admin.api.dto.response.AdminReservationTimeResponse;
import roomescape.controller.admin.api.dto.response.AdminThemeResponse;

public final class AdminReservationSlotResponseRowMapper {

    public static final RowMapper<AdminReservationSlotResponse> ADMIN_RESERVATION_SLOT_RESPONSE_ROW_MAPPER =
            (rs, rowNum) -> new AdminReservationSlotResponse(
                    rs.getLong("slot_id"),
                    rs.getDate("reservation_date").toLocalDate(),
                    new AdminThemeResponse(
                            rs.getLong("theme_id"),
                            rs.getString("theme_name"),
                            rs.getString("theme_description"),
                            rs.getString("theme_thumbnail_image_url"),
                            rs.getInt("theme_price"),
                            rs.getBoolean("theme_is_active")
                    ),
                    new AdminReservationTimeResponse(
                            rs.getLong("time_id"),
                            rs.getTime("time_start_at").toLocalTime(),
                            rs.getString("time_status")
                    ),
                    new AdminReservationResponse(
                            rs.getLong("reservation_id"),
                            rs.getString("reservation_name"),
                            rs.getString("reservation_status"),
                            rs.getTimestamp("reservation_created_at").toLocalDateTime()
                    )
            );

    private AdminReservationSlotResponseRowMapper() {
    }
}
