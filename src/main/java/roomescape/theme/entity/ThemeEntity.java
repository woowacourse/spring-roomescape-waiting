package roomescape.theme.entity;

import roomescape.theme.domain.Theme;

public record ThemeEntity(
        Long id,
        String name,
        String description,
        String thumbnail
) {
    public Theme toTheme() {
        return Theme.of(id, name, description, thumbnail);
    }
}
