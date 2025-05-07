package roomescape.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.domain.ReservationTime;

public record ReservationTimeGetResponse(
    Long id,
    @JsonFormat(pattern = "HH:mm") LocalTime startAt) {

    public static ReservationTimeGetResponse from(ReservationTime reservationTime) {
        return new ReservationTimeGetResponse(
            reservationTime.getId(),
            reservationTime.getStartAt());
    }
}
