package roomescape.feature.time.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalTime;

public record ReservationTimeResponseDto(Long id, @JsonFormat(pattern = "HH:mm") LocalTime startAt, boolean deleted) {

}
