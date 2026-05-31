package roomescape.persistence.jdbc.mapper;

import org.springframework.jdbc.core.RowMapper;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationSlot;
import roomescape.domain.ReservationStatus;

public final class ReservationRowMapper {

    public static RowMapper<Reservation> reservationRowMapper(ReservationSlot slot) {
        return (rs, rowNum) -> new Reservation(
                rs.getLong("reservation_id"),
                rs.getString("reservation_name"),
                slot,
                ReservationStatus.valueOf(rs.getString("reservation_status")),
                rs.getTimestamp("reservation_created_at").toLocalDateTime()
        );
    }

    private ReservationRowMapper() {
    }
}
