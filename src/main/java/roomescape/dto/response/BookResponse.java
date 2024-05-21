package roomescape.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;

public record BookResponse(@JsonFormat(pattern = "HH:mm") LocalTime startAt, Long timeId, Boolean alreadyBooked) {
}
