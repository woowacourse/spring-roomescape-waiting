package roomescape.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;
import roomescape.dto.business.ReservationTimeWithBookState;

public record ReservationTimeWithBookStateResponse(
        long id,
        @JsonFormat(pattern = "HH:mm") LocalTime startAt,
        boolean alreadyBooked
) {

    public ReservationTimeWithBookStateResponse(ReservationTimeWithBookState timeWithBookState) {
        this(timeWithBookState.id(), timeWithBookState.startAt(), timeWithBookState.alreadyBooked());
    }
}
