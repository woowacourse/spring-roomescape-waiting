package roomescape.reservation.dto;

import roomescape.reservation.domain.Theme;

public record ThemeResponse(
        Long id,
        String name,
        String description,
        String thumbnail
) {

    public static ThemeResponse toResponse(Theme theme) {
        return new ThemeResponse(theme.getId(), theme.getName(), theme.getDescription(), theme.getThumbnail());
    }
}
