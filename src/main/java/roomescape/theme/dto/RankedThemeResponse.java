package roomescape.theme.dto;

import roomescape.theme.domain.Theme;

public record RankedThemeResponse(String name, String description, String thumbnail) {
    public static RankedThemeResponse from(final Theme theme) {
        return new RankedThemeResponse(
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnail()
        );
    }
}
