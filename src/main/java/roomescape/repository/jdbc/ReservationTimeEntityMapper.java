package roomescape.repository.jdbc;

import org.springframework.jdbc.core.RowMapper;
import roomescape.domain.ReservationTime;
import roomescape.domain.TimeStatus;

public final class ReservationTimeEntityMapper {

    public static final RowMapper<ReservationTime> RESERVATION_TIME_MAPPER = (rs, rowNum) -> ReservationTime.restore(
            rs.getLong("id"),
            rs.getTime("start_at").toLocalTime(),
            TimeStatus.valueOf(rs.getString("status"))
    );

    private ReservationTimeEntityMapper() {
    }
}
