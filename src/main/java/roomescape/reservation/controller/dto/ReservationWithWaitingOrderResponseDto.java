package roomescape.reservation.controller.dto;

import roomescape.reservation.domain.Status;
import roomescape.reservation.repository.dto.ReservationWithWaitingOrder;
import roomescape.theme.controller.dto.ThemeResponseDto;

public record ReservationWithWaitingOrderResponseDto(
        Long id,
        String name,
        ReservationTimeResponseDto time,
        ThemeResponseDto theme,
        Status status,
        Integer waitingOrder
) {
    public static ReservationWithWaitingOrderResponseDto from(ReservationWithWaitingOrder reservation) {
        return new ReservationWithWaitingOrderResponseDto(
                reservation.id(),
                reservation.name(),
                reservation.time(),
                reservation.theme(),
                reservation.status(),
                reservation.waitingOrder()
        );
    }
}
