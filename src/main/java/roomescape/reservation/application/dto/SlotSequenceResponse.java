package roomescape.reservation.application.dto;

import roomescape.reservation.domain.ReservationSlot;

public record SlotSequenceResponse(ReservationSlot slot,
                                   int sequence) {

}
