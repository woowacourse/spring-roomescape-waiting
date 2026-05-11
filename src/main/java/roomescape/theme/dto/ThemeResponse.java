package roomescape.theme.dto;

import roomescape.theme.Theme;

public record ThemeResponse(
        Long id,
        String name,
        String description,
        String image
) {
    public static ThemeResponse from(Theme theme) {
        return new ThemeResponse(theme.getId(), theme.getName(), theme.getDescription(), theme.getImage());
    }
}
