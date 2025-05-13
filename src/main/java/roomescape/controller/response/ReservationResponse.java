package roomescape.controller.response;

import roomescape.service.result.ReservationResult;

import java.time.LocalDate;

public record ReservationResponse(
        Long id,
        LoginMemberResponse member,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme
) {


    public static ReservationResponse from(ReservationResult reservationResult) {
        return new ReservationResponse(
                reservationResult.id(),
                LoginMemberResponse.from(reservationResult.memberResult()),
                reservationResult.date(),
                ReservationTimeResponse.from(reservationResult.time()),
                ThemeResponse.from(reservationResult.theme())
        );
    }
}
