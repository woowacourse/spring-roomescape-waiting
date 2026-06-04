package roomescape.reservation.presentation.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.reservation.domain.ReservationTime;

public record CreateTimeResponse(
    Long id,
    @JsonFormat(pattern = "HH:mm")
    LocalTime startAt
) {

    public static CreateTimeResponse from(ReservationTime reservationTime) {
        return new CreateTimeResponse(
            reservationTime.getId(),
            reservationTime.getStartAt()
        );
    }
}
