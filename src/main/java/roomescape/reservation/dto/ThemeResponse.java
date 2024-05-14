package roomescape.reservation.dto;

import roomescape.reservation.model.Theme;

public record ThemeResponse(
        Long id,
        String name,
        String description,
        String thumbnail
) {
    public static ThemeResponse from(final Theme theme) {
        return new ThemeResponse(
                theme.getId(),
                theme.getName().value(),
                theme.getDescription().value(),
                theme.getThumbnail().value()
        );
    }
}
