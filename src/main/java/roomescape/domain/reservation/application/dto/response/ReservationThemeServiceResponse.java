package roomescape.domain.reservation.application.dto.response;

import roomescape.domain.reservation.model.entity.ReservationTheme;

public record ReservationThemeServiceResponse(
        Long id,
        String name,
        String description,
        String thumbnail
) {

    public static ReservationThemeServiceResponse from(ReservationTheme reservationTheme) {
        return new ReservationThemeServiceResponse(reservationTheme.getId(), reservationTheme.getName(),
                reservationTheme.getDescription(), reservationTheme.getThumbnail());
    }
}
