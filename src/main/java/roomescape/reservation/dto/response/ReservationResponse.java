package roomescape.reservation.dto.response;

import java.time.LocalDate;
import roomescape.member.dto.response.ReservationMemberResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationTime.dto.response.ReservationTimeResponse;
import roomescape.theme.dto.response.ThemeResponse;

public record ReservationResponse(Long id, ReservationMemberResponse member, LocalDate date,
                                  ReservationTimeResponse time,
                                  ThemeResponse theme) {
    public static ReservationResponse from(final Reservation reservation) {
        return new ReservationResponse(
                reservation.getId(),
                new ReservationMemberResponse(reservation.getName()),
                reservation.getDate(),
                new ReservationTimeResponse(
                        reservation.getTimeId(),
                        reservation.getReservationTime()
                ),
                new ThemeResponse(reservation.getThemeId(),
                        reservation.getThemeName(),
                        reservation.getThemeDescription(),
                        reservation.getThemeThumbnail())
        );
    }
}
