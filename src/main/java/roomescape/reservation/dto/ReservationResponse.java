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
        reservation.getDate();
        return new ReservationResponse(
                reservation.getId(),
                new MemberResponse(
                        reservation.getMember().getId(),
                        reservation.getMember().getName().getValue(),
                        reservation.getMember().getEmail().getValue()
                ),
                reservation.getDate().getValue(),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme())
        );
    }
}
