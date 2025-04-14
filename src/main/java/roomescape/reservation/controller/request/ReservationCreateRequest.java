package roomescape.reservation.controller.request;

import java.time.LocalDate;

public record ReservationCreateRequest(
        Long memberId,
        LocalDate date,
        Long timeId,
        Long themeId
) {
}
