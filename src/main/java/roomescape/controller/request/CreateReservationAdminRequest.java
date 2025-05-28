package roomescape.controller.request;

import java.time.LocalDate;
import roomescape.domain.ReservationStatus;

public record CreateReservationAdminRequest(
        LocalDate date,
        Long themeId,
        Long timeId,
        Long memberId,
        ReservationStatus status) {
}
