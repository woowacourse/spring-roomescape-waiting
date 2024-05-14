package roomescape.service.mapper;

import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.ReservationResponse;
import roomescape.dto.ReservationTimeResponse;
import roomescape.dto.ThemeResponse;

public class ReservationResponseMapper {
    public static ReservationResponse toResponse(Reservation reservation) {
        ReservationTime reservationTime = reservation.getReservationTime();
        ReservationTimeResponse reservationTimeResponse = ReservationTimeResponseMapper.toResponse(reservationTime);
        Theme theme = reservation.getTheme();
        ThemeResponse themeResponse = ThemeResponseMapper.toResponse(theme);
        return new ReservationResponse(reservation.getId(),
                reservation.getName(), reservation.getDate(), reservationTimeResponse, themeResponse);
    }
}
