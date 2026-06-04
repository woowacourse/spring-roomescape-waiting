package roomescape.service.dto.response;

import roomescape.domain.Theme;
import roomescape.repository.dto.ThemeDto;

public record ServiceThemeResponse(
        Long id,
        String name,
        String description,
        String thumbnailUrl
) {
    public static ServiceThemeResponse from(Theme theme) {
        return new ServiceThemeResponse(
                theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnailUrl()
        );
    }

    public static ServiceThemeResponse from(ThemeDto themeDto) {
        return new ServiceThemeResponse(
                themeDto.id(),
                themeDto.name(),
                themeDto.description(),
                themeDto.thumbnailUrl()
        );
    }
}
