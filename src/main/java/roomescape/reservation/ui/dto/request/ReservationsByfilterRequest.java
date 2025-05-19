package roomescape.reservation.ui.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ReservationsByfilterRequest(

        @NotNull
        Long themeId,
        @NotNull
        Long memberId,
        @NotNull
        LocalDate dateFrom,
        @NotNull
        LocalDate dateTo
) {

}
