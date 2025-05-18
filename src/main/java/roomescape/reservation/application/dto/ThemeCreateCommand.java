package roomescape.reservation.application.dto;

import roomescape.reservation.domain.theme.Theme;

public record ThemeCreateCommand(String name, String description, String thumbnail) {

    public Theme convertToTheme() {
        return new Theme(null, name, description, thumbnail);
    }
}
