package roomescape.dto.response;

import java.time.LocalDate;
import roomescape.domain.Reservation;
import roomescape.dto.projection.ReservationOrderProjection;

public record ReservationOrderResponse(Long id, String name, LocalDate date, ReservationTimeResponse timeResponse,
                                       ThemeResponse themeResponse, Long order, String status) {

    public static ReservationOrderResponse from(ReservationOrderProjection reservation) {
        return new ReservationOrderResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme()),
                reservation.getOrder(),
                toStatus(reservation.getOrder())
        );
    }

    public static ReservationOrderResponse from(Reservation reservation, long order) {
        return new ReservationOrderResponse(
                reservation.getId(),
                reservation.getName(),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme()),
                order,
                toStatus(order)
        );
    }

    private static String toStatus(long order) {
        if (order == 0) {
            return "예약";
        }
        return order + "번째 예약대기";
    }
}
