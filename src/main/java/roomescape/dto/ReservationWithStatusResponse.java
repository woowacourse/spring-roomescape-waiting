package roomescape.dto;

import java.time.LocalDate;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationWithStatus;

public record ReservationWithStatusResponse(
        Long id,
        String name,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        ReservationStatus status,
        Integer waitingOrder
) {
    public static ReservationWithStatusResponse from(ReservationWithStatus reservationWithStatus) {
        return new ReservationWithStatusResponse(
                reservationWithStatus.getId(),
                reservationWithStatus.getName(),
                reservationWithStatus.getDate(),
                ReservationTimeResponse.from(reservationWithStatus.getTime(), true),
                ThemeResponse.from(reservationWithStatus.getTheme()),
                reservationWithStatus.getStatus(),
                reservationWithStatus.getWaitingOrder()
        );
    }
}
