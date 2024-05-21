package roomescape.dto.response;

import java.time.LocalDate;
import roomescape.domain.reservation.Reservation;

public record PersonalReservationResponse(
        Long id,
        LocalDate date,
        MemberResponse member,
        ReservationTimeResponse time,
        ThemeResponse theme,
        String status
) {

    public static PersonalReservationResponse from(Reservation reservation) {
        return new PersonalReservationResponse(
                reservation.getId(),
                reservation.getDate(),
                MemberResponse.from(reservation.getMember()),
                ReservationTimeResponse.from(reservation.getTime()),
                ThemeResponse.from(reservation.getTheme()),
                reservation.getStatus().getName()
        );
    }
}
