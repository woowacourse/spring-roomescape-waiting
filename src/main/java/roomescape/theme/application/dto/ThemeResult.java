package roomescape.theme.application.dto;

import roomescape.theme.domain.Theme;

public record ThemeResult(Long id, String name, String description, String thumbnailImgUrl) {
    public static ThemeResult from(Theme theme) {
        return new ThemeResult(
                theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnailImgUrl()
        );
    }

    public static ThemeResult from(Long id, String name, String description, String thumbnailImgUrl) {
        return new ThemeResult(
                id,
                name,
                description,
                thumbnailImgUrl
        );
    }
}
