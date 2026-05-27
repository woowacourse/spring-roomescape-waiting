package roomescape.theme.repository;

import roomescape.theme.domain.Theme;
import roomescape.theme.repository.entity.ThemeEntity;

public class ThemeMapper {

    public static Theme toDomain(ThemeEntity entity) {
        return new Theme(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getThumbnailUrl()
        );
    }

    public static ThemeEntity toEntity(Theme domain) {
        return new ThemeEntity(
                domain.getId(),
                domain.getName(),
                domain.getDescription(),
                domain.getThumbnailUrl()
        );
    }
}
