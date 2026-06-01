package roomescape.service.dto;

import roomescape.domain.Theme;

public record ThemeResult(
        Long id,
        String name,
        String description,
        String thumbnailUrl
) {
    public static ThemeResult from(Theme theme) {
        return new ThemeResult(theme.getId(), theme.getName(), theme.getDescription(), theme.getThumbnailUrl());
    }
}
