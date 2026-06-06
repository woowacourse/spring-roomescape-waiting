package roomescape.service.dto;

import roomescape.domain.Theme;
import roomescape.repository.dto.ThemeDto;

public record ThemeInfo(
        Long id,
        String name,
        String description,
        String thumbnailUrl
) {
    public static ThemeInfo from(ThemeDto themeDto) {
        return new ThemeInfo(
                themeDto.id(),
                themeDto.name(),
                themeDto.description(),
                themeDto.thumbnailUrl()
        );
    }

    public static ThemeInfo from(Theme theme) {
        return new ThemeInfo(
                theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnailUrl()
        );
    }

    public Theme toEntity() {
        return new Theme(id, name, description, thumbnailUrl);
    }
}
