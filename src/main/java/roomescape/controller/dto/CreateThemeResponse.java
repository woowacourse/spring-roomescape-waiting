package roomescape.controller.dto;

import roomescape.domain.theme.Theme;

public record CreateThemeResponse(Long id, String name, String description, String thumbnail) {

    public static CreateThemeResponse from(Theme theme) {
        return new CreateThemeResponse(
            theme.getId(),
            theme.getName(),
            theme.getDescription(),
            theme.getThumbnail()
        );
    }
}
