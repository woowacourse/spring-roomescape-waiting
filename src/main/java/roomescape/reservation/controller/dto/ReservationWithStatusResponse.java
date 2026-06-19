package roomescape.reservation.controller.dto;

import java.time.LocalDate;
import roomescape.reservation.query.dto.ReservationWithStatusResult;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

public record ReservationWithStatusResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTime time,
        Theme theme,
        String status,
        Long waitingOrder,
        String orderId
) {

    public static ReservationWithStatusResponse from(ReservationWithStatusResult result) {
        return new ReservationWithStatusResponse(
                result.id(),
                result.name(),
                result.date(),
                result.time(),
                result.theme(),
                result.status().name().toLowerCase(),
                result.waitingOrder(),
                result.orderId()
        );
    }
}
