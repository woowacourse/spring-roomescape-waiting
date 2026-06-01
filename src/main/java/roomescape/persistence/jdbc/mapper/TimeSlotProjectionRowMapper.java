package roomescape.persistence.jdbc.mapper;

import org.springframework.jdbc.core.RowMapper;
import roomescape.controller.client.api.dto.response.TimeSlotResponse;

public final class TimeSlotProjectionRowMapper {

    public static final RowMapper<TimeSlotResponse> TIME_SLOT_PROJECTION_ROW_MAPPER = (rs, rowNum) ->
            new TimeSlotResponse(
                    rs.getLong("time_id"),
                    rs.getTime("time_start_at").toLocalTime(),
                    rs.getBoolean("is_reservable")
            );

    private TimeSlotProjectionRowMapper() {
    }
}
