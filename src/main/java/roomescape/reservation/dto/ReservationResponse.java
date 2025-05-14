package roomescape.reservation.dto;

import java.time.LocalDate;

import roomescape.member.dto.MemberResponse;
import roomescape.reservation.domain.Reservation;

public record ReservationResponse(
        Long id,
        MemberResponse member,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme
) {

    public static ReservationResponse from(final Reservation reservation) {
        ReservationTimeResponse reservationTimeResponse = ReservationTimeResponse.from(
                reservation.getReservationTime()
        );
        ThemeResponse themeResponse = ThemeResponse.from(reservation.getTheme());
        MemberResponse memberResponse = MemberResponse.fromEntity(reservation.getMember());

        return new ReservationResponse(reservation.getId(),
                memberResponse,
                reservation.getReservationDate(),
                reservationTimeResponse,
                themeResponse
        );
    }
}
