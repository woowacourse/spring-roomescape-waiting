package roomescape.controller.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalTime;

public record TimeRequest(
    @JsonFormat(pattern = "HH:mm") @NotNull LocalTime startAt
) {
}
