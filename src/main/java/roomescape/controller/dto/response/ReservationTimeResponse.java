package roomescape.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.domain.ReservationTime;

import java.time.LocalTime;

public record ReservationTimeResponse(
        Long id,
        @JsonFormat(pattern = "HH:mm")
        LocalTime startAt
) {

    public static ReservationTimeResponse from(ReservationTime time) {
        return new ReservationTimeResponse(
                time.getId(),
                time.getStartAt()
        );
    }
}
