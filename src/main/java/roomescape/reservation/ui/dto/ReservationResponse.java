package roomescape.reservation.ui.dto;

import java.time.LocalDate;
import roomescape.member.ui.dto.MemberResponse;
import roomescape.reservation.application.dto.ReservationInfo;

public record ReservationResponse(
        Long id,
        MemberResponse member,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme
) {

    public ReservationResponse(final ReservationInfo reservationInfo) {
        this(reservationInfo.id(),
                new MemberResponse(reservationInfo.member()),
                reservationInfo.date(),
                new ReservationTimeResponse(reservationInfo.time()),
                new ThemeResponse(reservationInfo.theme())
        );
    }
}
