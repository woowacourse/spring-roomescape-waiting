package roomescape.persistence.jdbc.mapper;

import org.springframework.jdbc.core.RowMapper;
import roomescape.controller.client.api.dto.response.TimeSlotProjection;

public final class TimeSlotProjectionRowMapper {

    public static final RowMapper<TimeSlotProjection> TIME_SLOT_PROJECTION_ROW_MAPPER = (rs, rowNum) ->
            new TimeSlotProjection(
                    rs.getLong("time_id"),
                    rs.getTime("time_start_at").toLocalTime(),
                    rs.getBoolean("is_reservable")
            );

    private TimeSlotProjectionRowMapper() {
    }
}
