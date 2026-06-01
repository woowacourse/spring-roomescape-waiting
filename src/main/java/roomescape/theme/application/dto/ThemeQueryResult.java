package roomescape.theme.application.dto;

import roomescape.theme.domain.Theme;

public record ThemeQueryResult(Long id, String name, String description, String thumbnailImgUrl) {
    public static ThemeQueryResult from(Theme theme) {
        return new ThemeQueryResult(
                theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnailImgUrl()
        );
    }

    public static ThemeQueryResult from(Long id, String name, String description, String thumbnailImgUrl) {
        return new ThemeQueryResult(
                id,
                name,
                description,
                thumbnailImgUrl
        );
    }
}
