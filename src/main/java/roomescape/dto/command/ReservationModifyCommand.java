package roomescape.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record ReservationModifyCommand(
        @NotNull(message = "RESERVATION_ID_NULL")
        Long reservationId,

        @NotNull(message = "PERSON_NAME_NULL_OR_BLANK")
        @NotBlank(message = "PERSON_NAME_NULL_OR_BLANK")
        String name,

        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate date,

        Long timeId,

        Long themeId
) {
}
