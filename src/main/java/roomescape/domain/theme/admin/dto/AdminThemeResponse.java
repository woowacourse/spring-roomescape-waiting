package roomescape.domain.theme.admin.dto;

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
            theme.getContent(),
            theme.getUrl()
        );
    }
}
