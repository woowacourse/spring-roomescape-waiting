package roomescape.reservation.ui.waiting.dto;

import java.time.LocalDate;
import roomescape.member.ui.dto.MemberResponse;
import roomescape.reservation.application.waiting.dto.ReservationWaitingInfo;
import roomescape.reservation.ui.theme.dto.ThemeResponse;
import roomescape.reservation.ui.time.dto.ReservationTimeResponse;

public record ReservationWaitingResponse(
        long id,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        MemberResponse member
) {

    public ReservationWaitingResponse(final ReservationWaitingInfo reservationWaitingInfo) {
        this(reservationWaitingInfo.id(),
                reservationWaitingInfo.date(),
                new ReservationTimeResponse(reservationWaitingInfo.time()),
                new ThemeResponse(reservationWaitingInfo.theme()),
                new MemberResponse(reservationWaitingInfo.member())
        );
    }
}
