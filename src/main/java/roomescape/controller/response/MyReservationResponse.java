package roomescape.controller.response;

import java.time.LocalDate;
import roomescape.domain.ReservationStatus;
import roomescape.service.result.ReservationResult;

public record MyReservationResponse(
        Long id,
        LoginMemberResponse member,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        ReservationStatus status
) {

    public static MyReservationResponse from(ReservationResult reservationResult) {
        return new MyReservationResponse(
                reservationResult.id(),
                LoginMemberResponse.from(reservationResult.memberResult()),
                reservationResult.date(),
                ReservationTimeResponse.from(reservationResult.time()),
                ThemeResponse.from(reservationResult.theme()),
                reservationResult.status()
        );
    }
}
