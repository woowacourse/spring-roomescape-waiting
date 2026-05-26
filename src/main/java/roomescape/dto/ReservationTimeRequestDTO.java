package roomescape.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record ReservationTimeRequestDTO(
        @NotNull
        @JsonFormat(pattern = "HH:mm")
        LocalTime startAt
) {

}
