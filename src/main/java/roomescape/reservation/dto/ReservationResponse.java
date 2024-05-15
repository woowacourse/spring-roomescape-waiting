package roomescape.reservation.dto;

import roomescape.member.dto.MemberResponse;
import roomescape.reservation.model.Reservation;

import java.time.LocalDate;

public record ReservationResponse(
        Long id,
        MemberResponse member,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme
) {
    public static ReservationResponse from(final Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                new MemberResponse(
                        reservation.getMember().getId(),
                        reservation.getMember().getName().getName(),
                        reservation.getMember().getEmail().getEmail()
                ),
                reservation.getDate().getDate(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme())
        );
    }
}
