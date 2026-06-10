package roomescape.dto.response;

import java.time.LocalDate;
import roomescape.dto.projection.ReservationOrderProjection;

public record ReservationOrderResponse(Long id, String name, LocalDate date, ReservationTimeResponse timeResponse,
                                       ThemeResponse themeResponse, Long order) {
    public static ReservationOrderResponse from(ReservationOrderProjection reservation) {
        return new ReservationOrderResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme()),
                reservation.getOrder()
        );
    }
}
