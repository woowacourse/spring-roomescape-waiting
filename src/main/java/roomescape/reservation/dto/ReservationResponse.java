package roomescape.reservation.dto;

import roomescape.member.dto.MemberResponse;

import java.time.LocalDate;

public record ReservationResponse(
        Long id,
        MemberResponse member,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme
) {
    public static ReservationResponse from(final ReservationDto reservation) {
        return new ReservationResponse(
                reservation.id(),
                new MemberResponse(
                        reservation.member().id(),
                        reservation.member().name().getValue(),
                        reservation.member().email().getValue()
                ),
                reservation.date().getValue(),
                ReservationTimeResponse.from(reservation.time()),
                ThemeResponse.from(reservation.theme())
        );
    }
}
