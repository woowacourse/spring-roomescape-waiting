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
                        reservation.getMember().getName().value(),
                        reservation.getMember().getEmail().value()
                ),
                reservation.getDate().value(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme())
        );
    }
}
