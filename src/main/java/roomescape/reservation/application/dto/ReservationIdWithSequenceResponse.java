package roomescape.reservation.application.dto;

import roomescape.reservation.domain.ReservationId;

public record ReservationIdWithSequenceResponse(ReservationId reservationId,
                                                int sequence) {

}
