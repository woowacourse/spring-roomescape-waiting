package roomescape.repository.dto;

import roomescape.domain.Theme;

public record ThemeDto(
        Long id,
        String name,
        String description,
        String thumbnailUrl
) {
    public static ThemeDto from(Theme theme) {
        return new ThemeDto(
                theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnailUrl()
        );
    }
}
