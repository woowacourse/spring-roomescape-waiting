package roomescape.service.dto.response.reservationwait;

import java.time.LocalDate;
import roomescape.domain.reservationwait.ReservationWait;
import roomescape.service.dto.response.member.MemberIdAndNameResponse;
import roomescape.service.dto.response.reservationTime.ReservationTimeResponse;
import roomescape.service.dto.response.theme.ThemeResponse;

public record ReservationWaitResponse(Long id,
                                      MemberIdAndNameResponse member,
                                      LocalDate date,
                                      ReservationTimeResponse time,
                                      ThemeResponse theme) {

    public ReservationWaitResponse(ReservationWait reservationWait) {
        this(reservationWait.getId(),
                new MemberIdAndNameResponse(reservationWait.getMember().getId(),
                        reservationWait.getMember().getName().getValue()),
                reservationWait.getDate(),
                new ReservationTimeResponse(reservationWait.getTime()),
                new ThemeResponse(reservationWait.getTheme()));

    }
}
