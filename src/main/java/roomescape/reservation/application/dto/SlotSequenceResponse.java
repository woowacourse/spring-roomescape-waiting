package roomescape.reservation.application.dto;

import roomescape.reservation.domain.ReservationId;
import roomescape.reservation.domain.ReservationSlot;

public record SlotSequenceResponse(ReservationId reservationId,
                                   ReservationSlot slot,
                                   int sequence) {

}
