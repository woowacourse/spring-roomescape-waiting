package roomescape.reservation.ui.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.reservation.domain.BookingState;

public record AdminCreateReservationRequest(
        @NotNull
        Long memberId,
        @NotNull
        LocalDate date,
        @NotNull
        Long timeId,
        @NotNull
        Long themeId,
        @NotNull
        BookingState status
) {
}
