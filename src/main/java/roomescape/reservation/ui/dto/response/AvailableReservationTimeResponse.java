package roomescape.reservation.ui.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;

public record AvailableReservationTimeResponse(
        Long timeId,
        @JsonFormat(pattern = "HH:mm")
        LocalTime startAt,
        boolean alreadyBooked
) {

}
