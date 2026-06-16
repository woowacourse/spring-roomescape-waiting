package roomescape.service.result;

import roomescape.domain.Theme;

public record ThemeRegisterResult(
        long id,
        String name,
        String description,
        String thumbnailImageUrl,
        Long price,
        boolean isActive
) {

    public static ThemeRegisterResult from(Theme theme) {
        return new ThemeRegisterResult(
                theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnailImageUrl(),
                theme.getPrice(),
                theme.isActive());
    }
}
