package roomescape.theme.presentation.response;

import roomescape.theme.domain.Theme;

public record CreateThemeResponse(
    Long id,
    String name,
    String content,
    String url
) {

    public static CreateThemeResponse from(Theme theme) {
        return new CreateThemeResponse(
            theme.getId(),
            theme.getName(),
            theme.getContent(),
            theme.getUrl()
        );
    }
}
