package roomescape.service.dto.response.reservation;

import java.time.LocalDate;

import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationWait;
import roomescape.service.dto.response.member.MemberResponse;
import roomescape.service.dto.response.theme.ThemeResponse;
import roomescape.service.dto.response.time.ReservationTimeResponse;

public record ReservationResponse(Long id,
                                  LocalDate date,
                                  ReservationTimeResponse time,
                                  ThemeResponse theme,
                                  MemberResponse member) {

    public static ReservationResponse from(ReservationWait wait) {
        Reservation reservation = wait.getReservation();
        Member member = wait.getMember();
        return new ReservationResponse(
                reservation.getId(),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme()),
                new MemberResponse(member.getId(), member.getName())
        );
    }

    public static ReservationResponse from(Reservation reservation, String memberName) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getDate(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme()),
                new MemberResponse(memberName)
        );
    }
}
