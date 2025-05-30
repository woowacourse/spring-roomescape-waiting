package roomescape.theme.application.dto;

import roomescape.theme.domain.Theme;

public record ThemeCreateCommand(String name, String description, String thumbnail) {

    public Theme convertToEntity() {
        return new Theme(name, description, thumbnail);
    }
}
