package roomescape.presentation.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateReservationRequest(
    @JsonFormat(pattern = "yyyy-MM-dd")
    @NotNull
    LocalDate date,

    @NotNull
    Long timeId,

    @NotNull
    Long themeId
) {

}

