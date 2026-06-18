package roomescape.reservation.repository.dto;

import roomescape.reservation.controller.dto.ReservationTimeResponse;
import roomescape.reservation.domain.Status;
import roomescape.theme.controller.dto.ThemeResponse;

public record ReservationWithWaitingOrder(
        Long id,
        String name,
        ReservationTimeResponse time,
        ThemeResponse theme,
        Status status,
        Integer waitingOrder,
        String orderId,
        Long amount,
        String paymentKey
) {
}
