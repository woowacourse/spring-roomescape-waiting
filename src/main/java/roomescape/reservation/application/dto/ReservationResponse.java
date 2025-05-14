package roomescape.reservation.application.dto;

import java.time.LocalDate;
import roomescape.member.application.dto.MemberResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.theme.application.dto.ThemeResponse;

public record ReservationResponse(
        Long id,
        LocalDate date,
        ReservationTimeResponse time,
        ThemeResponse theme,
        MemberResponse member) {

    public static ReservationResponse of(final Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getDate(),
                ReservationTimeResponse.of(reservation.getTime()),
                ThemeResponse.of(reservation.getTheme()),
                MemberResponse.of(reservation.getMember())
        );
    }
}
