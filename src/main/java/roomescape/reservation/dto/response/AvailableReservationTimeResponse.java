package roomescape.reservation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.reservation.domain.ReservationTime;

import java.time.LocalTime;

public record AvailableReservationTimeResponse(
        Long id,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm", timezone = "Asia/Seoul")
        LocalTime startAt,
        boolean isReserved
) {

    public static AvailableReservationTimeResponse of(ReservationTime reservationTime, boolean isReserved) {
        return new AvailableReservationTimeResponse(reservationTime.getId(), reservationTime.getStartAt(), isReserved);
    }
}
