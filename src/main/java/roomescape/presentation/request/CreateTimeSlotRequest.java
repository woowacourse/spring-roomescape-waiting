package roomescape.presentation.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record CreateTimeSlotRequest(
    @JsonFormat(pattern = "HH:mm")
    @NotNull
    LocalTime startAt
) {

}
