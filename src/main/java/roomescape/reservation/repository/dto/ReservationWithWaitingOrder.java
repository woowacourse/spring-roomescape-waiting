package roomescape.reservation.repository.dto;

import roomescape.reservation.controller.dto.ReservationTimeResponseDto;
import roomescape.reservation.domain.Status;
import roomescape.theme.controller.dto.ThemeResponseDto;

public record ReservationWithWaitingOrder(
        Long id,
        String name,
        ReservationTimeResponseDto time,
        ThemeResponseDto theme,
        Status status,
        Integer waitingOrder
) {
}
