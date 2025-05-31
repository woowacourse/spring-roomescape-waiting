package roomescape.dto.theme;

import roomescape.domain.reservation.slot.Theme;

public record ThemeCreateRequestDto
        (String name,
         String description,
         String thumbnail
) {

    public Theme createWithoutId() {
        return new Theme(name, description, thumbnail);
    }
}
