package roomescape.theme.presentation.response;

import roomescape.theme.domain.Theme;

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
            theme.getContent(),
            theme.getUrl()
        );
    }
}
