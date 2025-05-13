package roomescape.theme.dto;

import roomescape.theme.domain.Theme;

public record ThemeResponse(
        long id,
        String name,
        String description,
        String thumbnail
) {

    public ThemeResponse(final Theme theme) {
        this(theme.getId(), theme.getName(), theme.getDescription(), theme.getThumbnail());
    }
}
