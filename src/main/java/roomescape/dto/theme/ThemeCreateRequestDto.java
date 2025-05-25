package roomescape.dto.theme;

import roomescape.domain.Theme;

public record ThemeCreateRequestDto
        (String name,
         String description,
         String thumbnail
) {

    public Theme createWithoutId() {
        return new Theme(name, description, thumbnail);
    }
}
