package roomescape.application.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record UserReservationCreateDto(

        @NotNull
        Long themeId,

        @JsonFormat(pattern = "yyyy-MM-dd")
        @NotNull
        LocalDate date,

        @NotNull
        Long timeId
) {
}
