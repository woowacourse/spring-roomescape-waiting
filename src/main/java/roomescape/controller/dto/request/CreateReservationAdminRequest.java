package roomescape.controller.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CreateReservationAdminRequest(

        @NotNull
        LocalDate date,

        @NotNull
        Long themeId,

        @NotNull
        Long timeId,

        @NotNull
        Long memberId
) {
}
