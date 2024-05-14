package roomescape.web.dto.response.reservation;

import java.time.LocalDate;

import roomescape.domain.Reservation;
import roomescape.web.dto.response.member.MemberResponse;
import roomescape.web.dto.response.theme.ThemeResponse;
import roomescape.web.dto.response.time.ReservationTimeResponse;

public record ReservationResponse(Long id,
                                  LocalDate date,
                                  ReservationTimeResponse time,
                                  ThemeResponse theme,
                                  MemberResponse member) {

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme()),
                new MemberResponse(reservation.getMember().getId(), reservation.getMember().getName())
        );
    }
}
