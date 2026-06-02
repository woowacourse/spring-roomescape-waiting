package roomescape.controller.reservationtime.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalTime;
import roomescape.domain.reservationtime.ReservationTimeSlot;
import roomescape.domain.reservationtime.ReservationTimeSlotStatus;

public record ReservationTimeSlotResponse(
        Long id,
        LocalTime startAt,
        ReservationTimeSlotStatus status,
        Long waitingId
) {

    public static ReservationTimeSlotResponse from(final ReservationTimeSlot reservationTimeSlot) {
        return from(reservationTimeSlot, null);
    }

    public static ReservationTimeSlotResponse from(
            final ReservationTimeSlot reservationTimeSlot,
            final Long waitingId
    ) {
        return new ReservationTimeSlotResponse(
                reservationTimeSlot.id(),
                reservationTimeSlot.startAt(),
                reservationTimeSlot.status(),
                waitingId
        );
    }

    @JsonProperty
    public boolean reservable() {
        return status == ReservationTimeSlotStatus.RESERVABLE;
    }

    @JsonProperty
    public boolean waitable() {
        return status == ReservationTimeSlotStatus.WAITABLE;
    }

    @JsonProperty
    public boolean past() {
        return status == ReservationTimeSlotStatus.PAST;
    }

    @JsonProperty
    public boolean waiting() {
        return waitingId != null;
    }
}
