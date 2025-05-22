package roomescape.dto.time;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;

public record AvailableReservationTimeResponse(long id, @JsonFormat(pattern = "HH:mm") LocalTime startAt,
                                               boolean alreadyBooked) {

}
