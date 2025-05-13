package roomescape.dto.response;

import roomescape.entity.Theme;

public record ThemeResponse(Long id, String description, String name, String thumbnail) {

    public static ThemeResponse from(Theme theme) {
        return new ThemeResponse(
                theme.getId(),
                theme.getDescription(),
                theme.getName(),
                theme.getThumbnail()
        );
    }
}
