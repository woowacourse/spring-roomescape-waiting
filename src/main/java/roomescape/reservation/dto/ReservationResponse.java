package roomescape.reservation.dto;

import roomescape.member.dto.MemberResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

import java.time.LocalDate;

public record ReservationResponse(Long id, MemberResponse member, LocalDate date, ReservationTime time, Theme theme) {

    public static ReservationResponse from(Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                MemberResponse.from(reservation.getMember()),
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme()
        );
    }
}
