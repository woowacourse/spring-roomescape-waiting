package roomescape.reservation.service.dto;

import roomescape.reservation.domain.Theme;

public record ThemeCreateCommand(String name, String description, String thumbnail) {

    public Theme convertToTheme() {
        return new Theme(null, name, description, thumbnail);
    }
}
