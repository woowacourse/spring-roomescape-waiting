package roomescape.presentation.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.business.model.vo.ReservationStatus;

public record ReservationRequest(
        @NotNull @FutureOrPresent LocalDate date,
        @NotBlank String timeId,
        @NotBlank String themeId,
        @NotNull ReservationStatus reservationStatus
) {
}
