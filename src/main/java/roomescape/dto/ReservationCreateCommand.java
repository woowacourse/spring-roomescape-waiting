package roomescape.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record ReservationCreateCommand(
        @NotNull(message = "PERSON_NAME_NULL_OR_BLANK")
        @NotBlank(message = "PERSON_NAME_NULL_OR_BLANK")
        String name,

        @DateTimeFormat(pattern = "yyyy-MM-dd")
        @NotNull(message = "DATE_NULL")
        LocalDate date,

        @NotNull(message = "TIME_ID_NULL")
        Long timeId,

        @NotNull(message = "THEME_ID_NULL")
        Long themeId
) {
}
