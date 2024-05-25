package roomescape.reservation.dto.response;

import roomescape.reservation.model.Theme;

public record FindThemeOfReservationResponse(Long id, String name, String description, String thumbnail) {

    public static FindThemeOfReservationResponse from(Theme theme) {
        return new FindThemeOfReservationResponse(
                theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnail());
    }
}
