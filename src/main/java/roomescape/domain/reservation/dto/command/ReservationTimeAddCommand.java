package roomescape.domain.reservation.dto.command;

import java.time.LocalTime;
import roomescape.domain.reservation.domain.reservationTime.ReservationTime;
import roomescape.domain.reservation.dto.request.ReservationTimeAddRequest;

public record ReservationTimeAddCommand(LocalTime startAt) {

    public static ReservationTimeAddCommand from(ReservationTimeAddRequest request) {
        return new ReservationTimeAddCommand(request.startAt());
    }

    public ReservationTime toEntity() {
        return new ReservationTime(null, startAt);
    }
}
