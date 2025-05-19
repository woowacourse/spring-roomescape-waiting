package roomescape.presentation.dto;

import roomescape.business.domain.Theme;

public record ThemeResponse(Long id, String name, String description, String thumbnail) {

    public static ThemeResponse from(final Theme theme) {
        return new ThemeResponse(theme.getId(), theme.getName(), theme.getDescription(), theme.getThumbnail());
    }
}
