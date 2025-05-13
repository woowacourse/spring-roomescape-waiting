package roomescape.controller.request;

import java.time.LocalDate;

public record CreateReservationAdminRequest(LocalDate reservationDate, Long themeId, Long timeId, Long memberId) {
}
