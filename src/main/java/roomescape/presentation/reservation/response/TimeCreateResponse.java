package roomescape.application.reservation.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.domain.reservation.ReservationTime;

public record TimeCreateResponse(
        Long id,
        @JsonFormat(pattern = "HH:mm")
        LocalTime startAt
) {

    public static TimeCreateResponse from(ReservationTime time) {
        return new TimeCreateResponse(
                time.getId(),
                time.getStartAt()
        );
    }
}
