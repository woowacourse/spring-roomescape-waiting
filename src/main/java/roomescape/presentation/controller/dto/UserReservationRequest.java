package roomescape.presentation.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record UserReservationRequest(
        @NotNull
        Long themeId,
        @JsonFormat(pattern = "yyyy-MM-dd")
        @NotNull
        LocalDate date,
        @NotNull
        Long timeId
) {
}
