package roomescape.business.dto;

import java.util.List;
import roomescape.business.model.entity.Theme;
import roomescape.business.model.vo.Id;
import roomescape.business.model.vo.ThemeName;

public record ThemeDto(
        Id id,
        ThemeName name,
        String description,
        String thumbnail
) {
    public static ThemeDto fromEntity(final Theme theme) {
        return new ThemeDto(
                theme.getId(),
                theme.getName(),
                theme.getDescription(),
                theme.getThumbnail()
        );
    }

    public static List<ThemeDto> fromEntities(final List<Theme> themes) {
        return themes.stream()
                .map(ThemeDto::fromEntity)
                .toList();
    }
}
