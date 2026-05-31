package roomescape.domain.reservationslot.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.domain.reservation.dto.ReservationCountResult;

public record ReservationSlotResponse(
    Long timeId,
    @JsonFormat(pattern = "HH:mm")
    LocalTime startAt,
    Long waitingNumber
) {

    public static ReservationSlotResponse from(ReservationCountResult reservationCountResult) {
        return new ReservationSlotResponse(
            reservationCountResult.timeId(),
            reservationCountResult.startAt(),
            reservationCountResult.waitingCount()
        );
    }
}
