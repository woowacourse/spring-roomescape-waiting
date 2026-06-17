package roomescape.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record ReservationCreateCommand(
        @NotNull(message = "PERSON_NAME_NULL_OR_BLANK")
        @NotBlank(message = "PERSON_NAME_NULL_OR_BLANK")
        String name,

        @NotNull(message = "AMOUNT_NULL")
        Long amount,

        @DateTimeFormat(pattern = "yyyy-MM-dd")
        @NotNull(message = "DATE_NULL")
        LocalDate date,

        @NotNull(message = "TIME_ID_NULL")
        Long timeId,

        @NotNull(message = "THEME_ID_NULL")
        Long themeId
) {
}
