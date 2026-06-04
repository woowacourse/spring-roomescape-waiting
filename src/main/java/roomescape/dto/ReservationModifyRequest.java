package roomescape.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record ReservationModifyRequest(
        @NotNull(message = "PERSON_NAME_NULL_OR_BLANK")
        @NotBlank(message = "PERSON_NAME_NULL_OR_BLANK")
        String name,

        @DateTimeFormat(pattern = "yyyy-MM-dd")
        LocalDate date,

        Long timeId
) {
}
