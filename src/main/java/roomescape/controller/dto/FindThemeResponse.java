package roomescape.controller.dto;

import roomescape.domain.theme.Theme;

public record FindThemeResponse(Long id, String name, String description, String thumbnail) {

    public static FindThemeResponse from(Theme theme) {
        return new FindThemeResponse(
            theme.getId(),
            theme.getName(),
            theme.getDescription(),
            theme.getThumbnail()
        );
    }
}
