package roomescape.reservation.dto;

import roomescape.reservation.domain.Theme;

public record PopularThemeResponse(
        String name,
        String description,
        String thumbnail
) {

    public static PopularThemeResponse toResponse(Theme theme) {
        return new PopularThemeResponse(theme.getName(), theme.getDescription(), theme.getThumbnail());
    }
}
