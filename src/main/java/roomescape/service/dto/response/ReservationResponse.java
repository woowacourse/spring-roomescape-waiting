package roomescape.service.dto.response;

import roomescape.domain.reservation.Reservation;

import java.time.LocalDate;

public record ReservationResponse(
        Long id,
        MemberResponse member,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme
) {

    public ReservationResponse(Reservation reservation) {
        this(
                reservation.getId(),
                MemberResponse.from(reservation.getMember()),
                reservation.getDate(),
                new ReservationTimeResponse(reservation.getTime()),
                new ThemeResponse(reservation.getTheme())
        );
    }
}
