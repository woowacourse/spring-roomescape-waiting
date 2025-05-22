package roomescape.presentation.api.reservation.response;

import java.time.LocalDate;
import roomescape.application.reservation.dto.ReservationResult;
import roomescape.presentation.api.member.MemberResponse;

public record ReservationResponse(
        Long id,
        MemberResponse member,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme
) {

    public static ReservationResponse from(ReservationResult reservationResult) {
        return new ReservationResponse(
                reservationResult.id(),
                MemberResponse.from(reservationResult.memberResult()),
                reservationResult.date(),
                ReservationTimeResponse.from(reservationResult.time()),
                ThemeResponse.from(reservationResult.theme())
        );
    }
}
