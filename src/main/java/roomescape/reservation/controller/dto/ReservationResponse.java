package roomescape.reservation.controller.dto;

import java.time.LocalDate;
import roomescape.member.controller.dto.MemberResponse;
import roomescape.reservation.service.dto.ReservationInfo;

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
