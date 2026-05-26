package roomescape.service.dto.response;

import roomescape.domain.Theme;

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
}
