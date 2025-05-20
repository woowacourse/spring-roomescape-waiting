package roomescape.theme.dto;

import roomescape.theme.domain.Theme;

public record PopularThemeResponse(
        String name,
        String description,
        String thumbnail
) {

    public PopularThemeResponse(final Theme theme) {
        this(theme.getName(), theme.getDescription(), theme.getThumbnail());
    }
}
