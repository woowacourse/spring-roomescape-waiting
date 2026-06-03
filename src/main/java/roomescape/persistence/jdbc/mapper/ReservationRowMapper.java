package roomescape.persistence.jdbc.mapper;

import org.springframework.jdbc.core.RowMapper;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationActiveStatus;
import roomescape.domain.ReservationStatus;

public final class ReservationRowMapper {

    public static final RowMapper<Reservation> RESERVATION_ROW_MAPPER = (rs, rowNum) ->
            new Reservation(
                    rs.getLong("reservation_id"),
                    rs.getString("reservation_name"),
                    ReservationStatus.valueOf(rs.getString("reservation_status")),
                    ReservationActiveStatus.valueOf(rs.getString("reservation_active_status")),
                    rs.getTimestamp("reservation_created_at").toLocalDateTime()
            );

    private ReservationRowMapper() {
    }
}
