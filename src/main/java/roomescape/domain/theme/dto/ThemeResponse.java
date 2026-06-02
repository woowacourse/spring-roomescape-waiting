package roomescape.domain.theme.dto;

import roomescape.domain.theme.Theme;

public record ThemeResponse(
    Long id,
    String name,
    String description,
    String imageUrl
) {

    public static ThemeResponse from(Theme theme) {
        return new ThemeResponse(
            theme.getId(),
            theme.getName(),
            theme.getDescription(),
            theme.getImageUrl()
        );
    }
}
