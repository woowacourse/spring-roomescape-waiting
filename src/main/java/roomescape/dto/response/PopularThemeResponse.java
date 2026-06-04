package roomescape.dto.response;

import roomescape.domain.PopularTheme;
import roomescape.domain.Theme;

public record PopularThemeResponse(
        Long id,
        String name,
        String description,
        String thumbnailImageUrl,
        Long reservedCount
) {
    public static PopularThemeResponse from(PopularTheme popularTheme) {
        Theme theme = popularTheme.getTheme();
        return new PopularThemeResponse(
                theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnailImageUrl(),
                popularTheme.getReservationCount()
        );
    }
}
