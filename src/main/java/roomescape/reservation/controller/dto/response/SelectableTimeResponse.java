package roomescape.reservation.controller.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;

public record SelectableTimeResponse(
        long id,

        @JsonFormat(pattern = "kk:mm")
        LocalTime startAt,

        boolean alreadyBooked
) {

}
