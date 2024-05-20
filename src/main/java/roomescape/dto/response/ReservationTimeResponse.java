package roomescape.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import roomescape.domain.ReservationTime;

import java.time.LocalTime;

public record ReservationTimeResponse(Long id, @JsonFormat(pattern = "HH:mm") LocalTime startAt) {

    public static ReservationTimeResponse from(ReservationTime reservationTime) {
        return new ReservationTimeResponse(
                reservationTime.getId(),
                reservationTime.getStartAt()
        );
    }
}
