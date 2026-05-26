package roomescape.reservation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;

public record ReservationTimeCreateResponse(
        Long id,

        @JsonFormat(pattern = "HH:mm")
        LocalTime startAt
) {

    public static ReservationTimeCreateResponse of(Long id, LocalTime startAt) {
        return new ReservationTimeCreateResponse(id, startAt);
    }
}
