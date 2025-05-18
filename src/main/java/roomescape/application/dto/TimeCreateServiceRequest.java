package roomescape.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;

public record TimeCreateServiceRequest(
        @JsonFormat(pattern = "HH:mm")
        @NotNull
        LocalTime startAt
) {
}
