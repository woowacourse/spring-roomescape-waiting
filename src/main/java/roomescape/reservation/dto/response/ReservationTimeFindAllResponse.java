package roomescape.reservation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;

public record ReservationTimeFindAllResponse(
        Long id,

        @JsonFormat(pattern = "HH:mm")
        LocalTime startAt
) {

    public static ReservationTimeFindAllResponse of(Long id, LocalTime startAt) {
        return new ReservationTimeFindAllResponse(id, startAt);
    }
}

