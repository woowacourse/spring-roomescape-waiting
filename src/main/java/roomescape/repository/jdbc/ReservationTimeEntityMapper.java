package roomescape.repository.jdbc;

import org.springframework.jdbc.core.RowMapper;
import roomescape.domain.ReservationTime;
import roomescape.domain.TimeStatus;
import roomescape.repository.dto.TimeSlotProjection;

public final class ReservationTimeEntityMapper {

    public static final RowMapper<ReservationTime> RESERVATION_TIME_MAPPER = (rs, rowNum) -> new ReservationTime(
            rs.getLong("id"),
            rs.getTime("start_at").toLocalTime(),
            TimeStatus.valueOf(rs.getString("status"))
    );

    public static final RowMapper<TimeSlotProjection> TIME_SLOT_PROJECTION_MAPPER = (rs, rowNum) -> new TimeSlotProjection(
            rs.getLong("time_id"),
            rs.getTime("time_start_at").toLocalTime(),
            rs.getBoolean("is_reservable")
    );

    private ReservationTimeEntityMapper() {}
}
