package roomescape.dto;

import roomescape.domain.Theme;

public record ThemeRequest(String name, String description, String thumbnail) {

    public ThemeRequest {
        InputValidator.validateNotNull(name, description, thumbnail);
        InputValidator.validateNotBlank(name, description, thumbnail);
    }

    public Theme toTheme() {
        return new Theme(name, description, thumbnail);
    }
}
