package roomescape.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.domain.reservation.ReservationTime;

public record FindTimeResponse(Long id, @JsonFormat(pattern = "HH:mm") LocalTime startAt) {

    public static FindTimeResponse from(ReservationTime time) {
        return new FindTimeResponse(
            time.getId(),
            time.getStartAt()
        );
    }
}
