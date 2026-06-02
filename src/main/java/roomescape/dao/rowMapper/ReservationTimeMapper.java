package roomescape.dao.rowMapper;

import org.springframework.jdbc.core.RowMapper;
import roomescape.dao.dto.TimeQueryResult;
import roomescape.domain.reservation.time.ReservationTime;

public final class ReservationTimeMapper {

    public static final RowMapper<ReservationTime> RESERVATION_TIME_ROW_MAPPER = (rs, rowNum) -> {
        return new ReservationTime(
                rs.getLong("id"),
                rs.getTime("start_at").toLocalTime()
        );
    };

    public static final RowMapper<TimeQueryResult> RESERVATION_TIME_STATUS_ROW_MAPPER = (rs, rowNum) -> {
        return new TimeQueryResult(new ReservationTime(
                rs.getLong("id"),
                rs.getTime("start_at").toLocalTime()
        ),
        rs.getBoolean("reservable"));
    };

    private ReservationTimeMapper() {
    }
}
