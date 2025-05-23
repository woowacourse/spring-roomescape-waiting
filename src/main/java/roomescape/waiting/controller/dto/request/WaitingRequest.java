package roomescape.waiting.controller.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record WaitingRequest(
    @JsonFormat(pattern = "yyyy-MM-dd") @NotNull LocalDate date,
    @NotNull long timeId,
    @NotNull long themeId
) {
}
