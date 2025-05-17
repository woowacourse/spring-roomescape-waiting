package roomescape.theme.application.dto;

import roomescape.theme.domain.Theme;

public record ThemeResponse(
        Long id,
        String name,
        String description,
        String thumbnail
) {

    public static ThemeResponse of(final Theme theme) {
        return new ThemeResponse(
                theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnail()
        );
    }
}
