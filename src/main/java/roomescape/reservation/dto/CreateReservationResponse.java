package roomescape.reservation.dto;

import java.time.LocalDate;
import roomescape.member.dto.MemberResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.theme.dto.ThemeResponse;

public record CreateReservationResponse(
        Long id,
        MemberResponse member,
        ThemeResponse theme,
        ReservationTimeResponse time,
        LocalDate date
) {

    public static CreateReservationResponse from(final Reservation reservation) {
        return new CreateReservationResponse(
                reservation.getId(),
                MemberResponse.from(reservation.getMember()),
                ThemeResponse.from(reservation.getTheme()),
                ReservationTimeResponse.from(reservation.getTime()),
                reservation.getDate()
        );
    }
}
