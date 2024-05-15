package roomescape.service.dto.response;

import java.time.LocalDate;
import roomescape.domain.Reservation;

public record MyReservationResponse(Long reservationId,
                                    LocalDate date,
                                    ReservationTimeResponse time,
                                    ThemeResponse theme,
                                    String status) {

    public static MyReservationResponse from(Reservation reservation) {
        return new MyReservationResponse(
                reservation.getId(),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme()),
                "예약"
        );
    }
}
