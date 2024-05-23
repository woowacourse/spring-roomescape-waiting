package roomescape.service.reservationwaiting.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import roomescape.exception.common.InvalidRequestBodyException;

public class ReservationWaitingRequest {
    private final Long reservationId;

    @JsonCreator
    public ReservationWaitingRequest(Long reservationId) {
        validate(reservationId);
        this.reservationId = reservationId;
    }

    private void validate(Long reservationId) {
        if (reservationId == null) {
            throw new InvalidRequestBodyException();
        }
    }

    public Long getReservationId() {
        return reservationId;
    }
}
