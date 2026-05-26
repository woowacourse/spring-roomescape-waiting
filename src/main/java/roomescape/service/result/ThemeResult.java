package roomescape.service.result;

import roomescape.domain.Theme;

public record ThemeResult(
        long id,
        String name,
        String description,
        String thumbnailImageUrl,
        boolean isActive
) {

    public static ThemeResult from(Theme theme) {
        return new ThemeResult(
                theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnailImageUrl(),
                theme.isActive());
    }
}
