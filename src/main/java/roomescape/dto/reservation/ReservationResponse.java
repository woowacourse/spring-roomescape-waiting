package roomescape.dto.reservation;

import roomescape.domain.reservation.Reservation;
import roomescape.dto.reservationtime.ReservationTimeResponse;
import roomescape.dto.theme.ThemeResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ReservationResponse(Long id, String name, LocalDate date, ReservationTimeResponse time, ThemeResponse theme, LocalDateTime createdAt, boolean paid) {

    public static ReservationResponse from(Reservation reservation) {
        ReservationTimeResponse reservationTimeResponse = ReservationTimeResponse.from(reservation.getTime());
        ThemeResponse themeResponse = ThemeResponse.from(reservation.getTheme());
        return new ReservationResponse(reservation.getId(), reservation.getName(), reservation.getDate(), reservationTimeResponse, themeResponse, reservation.getCreatedAt(), reservation.isPaid());
    }
}
