package roomescape.reservation.ui.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import roomescape.reservation.domain.ReservationStatus;

public record CreateReservationRequest(
) {

    public record ForMember(
            @NotNull
            LocalDate date,
            @NotNull
            Long timeId,
            @NotNull
            Long themeId
    ) {
    }

    public record ForAdmin(
            @NotNull
            Long memberId,
            @NotNull
            LocalDate date,
            @NotNull
            Long timeId,
            @NotNull
            Long themeId,
            @NotNull
            ReservationStatus status
    ) {
    }
}
