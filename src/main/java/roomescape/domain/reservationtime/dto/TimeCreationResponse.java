package roomescape.domain.reservationtime.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.domain.reservationtime.ReservationTime;

public record TimeCreationResponse(
    Long id,
    @JsonFormat(pattern = "HH:mm")
    LocalTime startAt
) {

    public static TimeCreationResponse from(ReservationTime reservationTime) {
        return new TimeCreationResponse(
            reservationTime.getId(),
            reservationTime.getStartAt()
        );
    }
}
