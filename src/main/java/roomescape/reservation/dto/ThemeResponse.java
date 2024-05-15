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
                theme.getName().getName(),
                theme.getDescription().getDescription(),
                theme.getThumbnail().getThumbnail()
        );
    }
}
