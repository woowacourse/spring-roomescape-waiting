package roomescape.reservation.dto.response;

import roomescape.theme.model.Theme;

public record CreateThemeOfReservationResponse(Long id,
                                               String name,
                                               String description,
                                               String thumbnail) {
    public static CreateThemeOfReservationResponse from(final Theme theme) {
        return new CreateThemeOfReservationResponse(
                theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnail());
    }
}
