package roomescape.presentation.dto;

import java.time.LocalDate;
import roomescape.business.domain.Reservation;

public record ReservationResponse(Long id, LocalDate date, MemberResponse member, PlayTimeResponse time, ThemeResponse theme) {

    public static ReservationResponse from(final Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                reservation.getDate(),
                MemberResponse.from(reservation.getMember()),
                PlayTimeResponse.from(reservation.getPlayTime()),
                ThemeResponse.from(reservation.getTheme())
        );
    }
}
