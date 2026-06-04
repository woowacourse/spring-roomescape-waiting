package roomescape.theme.presentation.response;

import roomescape.theme.domain.Theme;

public record AdminThemeResponse(
    Long id,
    String name,
    String content,
    String url
) {

    public static AdminThemeResponse from(Theme theme) {
        return new AdminThemeResponse(
            theme.getId(),
            theme.getName(),
            theme.getContent(),
            theme.getUrl()
        );
    }
}
