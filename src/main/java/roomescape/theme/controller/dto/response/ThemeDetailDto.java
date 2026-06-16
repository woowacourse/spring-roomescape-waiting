package roomescape.theme.controller.dto.response;

import roomescape.theme.domain.Theme;
import roomescape.theme.repository.projection.PopularThemeResult;

public record ThemeDetailDto(
        Long id,
        String name,
        String description,
        String thumbnailUrl,
        boolean isActive,
        Long amount
) {
    public static ThemeDetailDto from(Theme theme) {
        return new ThemeDetailDto(
                theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnailUrl(),
                theme.isActive(),
                theme.getAmount()
        );
    }

    public static ThemeDetailDto from(PopularThemeResult result) {
        return new ThemeDetailDto(
                result.id(),
                result.name(),
                result.description(),
                result.thumbnailUrl(),
                result.isActive(),
                result.amount()
        );
    }
}
