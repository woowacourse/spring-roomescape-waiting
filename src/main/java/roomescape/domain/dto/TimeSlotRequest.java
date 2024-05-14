package roomescape.domain.dto;

import java.time.LocalTime;
import roomescape.domain.ReservationTime;
import roomescape.exception.clienterror.EmptyValueNotAllowedException;

public record TimeSlotRequest(LocalTime startAt) {
    public TimeSlotRequest {
        isValid(startAt);
    }

    private void isValid(LocalTime startAt) {
        if (startAt == null) {
            throw new EmptyValueNotAllowedException("startAt");
        }
    }

    public ReservationTime toEntity(Long id) {
        return new ReservationTime(id, startAt);
    }
}
