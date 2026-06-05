package roomescape.waiting.application.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record WaitingRequest(
        @JsonFormat(pattern = "yyyy-MM-dd") @NotNull LocalDate date,
        @NotNull Long timeId,
        @NotNull Long themeId
) {
}
