package roomescape.service.result;

import roomescape.domain.Theme;

public record ThemeRegisterResult(
        long id,
        String name,
        String description,
        String thumbnailImageUrl,
        boolean isActive
) {

    public static ThemeRegisterResult from(Theme theme) {
        return new ThemeRegisterResult(
                theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnailImageUrl(),
                theme.isActive());
    }
}
