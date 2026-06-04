package roomescape.presentation.theme.response;

import roomescape.domain.theme.Theme;

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
            theme.getDescription(),
            theme.getThumbnailUrl()
        );
    }
}
