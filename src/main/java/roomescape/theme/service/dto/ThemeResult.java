package roomescape.theme.service.dto;

import roomescape.theme.domain.Theme;

public record ThemeResult(
        Long id,
        String name,
        String description,
        String thumbnailUrl
) {
    public static ThemeResult from(Theme theme) {
        return new ThemeResult(theme.id(), theme.name(), theme.description(), theme.thumbnailUrl());
    }
}
