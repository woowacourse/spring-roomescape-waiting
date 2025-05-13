package roomescape.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;

public record AvailableTimeResponse(Long id, @JsonFormat(pattern = "HH:mm") LocalTime startAt, boolean alreadyBooked) {
}
