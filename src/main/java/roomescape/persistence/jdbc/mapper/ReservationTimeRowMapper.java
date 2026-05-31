package roomescape.persistence.jdbc.mapper;

import org.springframework.jdbc.core.RowMapper;
import roomescape.domain.ReservationTime;
import roomescape.domain.TimeStatus;

public final class ReservationTimeRowMapper {

    public static final RowMapper<ReservationTime> RESERVATION_TIME_ROW_MAPPER = (rs, rowNum) -> new ReservationTime(
            rs.getLong("id"),
            rs.getTime("start_at").toLocalTime(),
            TimeStatus.valueOf(rs.getString("status"))
    );

    private ReservationTimeRowMapper() {
    }
}
