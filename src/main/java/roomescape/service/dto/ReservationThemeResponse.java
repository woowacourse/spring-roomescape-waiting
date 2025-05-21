package roomescape.service.dto;

import roomescape.domain.ReservationTheme;

public record ReservationThemeResponse(long id, String name, String description, String thumbnail) {

    public static ReservationThemeResponse from(ReservationTheme reservationTheme) {
        return new ReservationThemeResponse(reservationTheme.getId(), reservationTheme.getName(),
                reservationTheme.getDescription(), reservationTheme.getThumbnail());
    }
}
