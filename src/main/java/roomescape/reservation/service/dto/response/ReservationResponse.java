package roomescape.reservation.service.dto.response;

import java.time.LocalDate;

import roomescape.member.service.dto.response.MemberResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.theme.service.dto.response.ThemeResponse;

public record ReservationResponse(
        Long id,
        MemberResponse member,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme
) {

    public static ReservationResponse from(final Reservation reservation) {
        ReservationTimeResponse reservationTimeResponse = ReservationTimeResponse.from(
                reservation.getTime()
        );
        ThemeResponse themeResponse = ThemeResponse.from(reservation.getTheme());
        MemberResponse memberResponse = MemberResponse.fromEntity(reservation.getMember());

        return new ReservationResponse(reservation.getId(),
                memberResponse,
                reservation.getDate(),
                reservationTimeResponse,
                themeResponse
        );
    }
}
