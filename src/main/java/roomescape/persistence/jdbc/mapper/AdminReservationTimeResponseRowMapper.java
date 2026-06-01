package roomescape.persistence.jdbc.mapper;

import org.springframework.jdbc.core.RowMapper;
import roomescape.controller.admin.api.dto.response.AdminReservationTimeResponse;

public final class AdminReservationTimeResponseRowMapper {

    public static final RowMapper<AdminReservationTimeResponse> ADMIN_RESERVATION_TIME_RESPONSE_ROW_MAPPER =
            (rs, rowNum) -> new AdminReservationTimeResponse(
                    rs.getLong("id"),
                    rs.getTime("start_at").toLocalTime(),
                    rs.getString("status")
            );

    private AdminReservationTimeResponseRowMapper() {
    }
}
