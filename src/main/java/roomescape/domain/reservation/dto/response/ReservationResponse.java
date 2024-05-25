package roomescape.domain.reservation.dto.response;

import java.time.LocalDate;
import roomescape.domain.member.domain.Member;
import roomescape.domain.member.dto.MemberResponse;
import roomescape.domain.reservation.domain.reservation.Reservation;
import roomescape.domain.reservation.domain.reservationTime.ReservationTime;
import roomescape.domain.theme.domain.Theme;

public record ReservationResponse(Long id,
                                  LocalDate date,
                                  ReservationTime time,
                                  Theme theme,
                                  MemberResponse memberResponse) {
    public static ReservationResponse from(Reservation reservation) {
        Member member = reservation.getMember();
        MemberResponse memberResponse = new MemberResponse(
                member.getId(),
                member.getName(),
                member.getEmail(),
                member.getRole()
        );
        return new ReservationResponse(reservation.getId(),
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme(),
                memberResponse);
    }
}
