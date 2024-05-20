package roomescape.service.dto.response.reservation;

import java.time.LocalDate;
import roomescape.domain.ReservationDetail;
import roomescape.service.dto.response.theme.ThemeResponse;
import roomescape.service.dto.response.time.ReservationTimeResponse;
import roomescape.service.dto.response.member.MemberResponse;

public record ReservationResponse(Long id,
                                  LocalDate date,
                                  ReservationTimeResponse time,
                                  ThemeResponse theme,
                                  MemberResponse member) {

    public static ReservationResponse from(ReservationDetail reservationDetail) {
        return new ReservationResponse(
                reservationDetail.getId(),
                reservationDetail.getDate(),
                ReservationTimeResponse.from(reservationDetail.getTime()),
                ThemeResponse.from(reservationDetail.getTheme()),
                new MemberResponse(reservationDetail.getMember().getId(), reservationDetail.getMember().getName())
        );
    }
}
