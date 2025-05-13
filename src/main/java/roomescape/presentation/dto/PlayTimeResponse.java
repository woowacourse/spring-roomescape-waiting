package roomescape.presentation.dto;

import java.time.LocalTime;
import roomescape.business.domain.ReservationTime;

public record PlayTimeResponse(Long id, LocalTime startAt) {

    public static PlayTimeResponse from(final ReservationTime reservationTime) {
        return new PlayTimeResponse(reservationTime.getId(), reservationTime.getStartAt());
    }
}
