package roomescape.presentation.theme.response;

import roomescape.domain.theme.Theme;

public record ThemeResponse(
    Long id,
    String name,
    String content,
    String url
) {

    public static ThemeResponse from(Theme theme) {
        return new ThemeResponse(
            theme.getId(),
            theme.getName(),
            theme.getDescription(),
            theme.getThumbnailUrl()
        );
    }
}
