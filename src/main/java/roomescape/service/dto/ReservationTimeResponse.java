package roomescape.service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.domain.ReservationTime;

public record ReservationTimeResponse(
        Long id,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm", timezone = "Asia/Seoul")
        LocalTime startAt
) {

    public ReservationTimeResponse(ReservationTime reservationTime) {
        this(reservationTime.getId(), reservationTime.getStartAt());
    }
}
