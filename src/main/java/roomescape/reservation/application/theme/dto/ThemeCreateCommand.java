package roomescape.reservation.application.theme.dto;

import roomescape.reservation.domain.theme.Theme;

public record ThemeCreateCommand(String name, String description, String thumbnail) {

    public Theme convertToEntity() {
        return new Theme(name, description, thumbnail);
    }
}
