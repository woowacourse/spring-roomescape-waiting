package roomescape.dto.response;

import java.time.LocalDate;
import roomescape.domain.ReservationOrder;
import roomescape.domain.ReservationStatus;

public record ReservationOrderResponse(Long id, String name, LocalDate date, ReservationTimeResponse timeResponse,
                                       ThemeResponse themeResponse, ReservationStatus status, Long order) {
    public static ReservationOrderResponse from(ReservationOrder reservation) {
        return new ReservationOrderResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme()),
                reservation.getStatus(),
                reservation.getOrder()
        );
    }
}
