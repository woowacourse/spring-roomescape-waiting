package roomescape.dto.request;

import roomescape.domain.Theme;

import static roomescape.dto.InputValidator.validateNotBlank;
import static roomescape.dto.InputValidator.validateNotNull;

public record ThemeRequest(String name, String description, String thumbnail) {

    public ThemeRequest {
        validateNotNull(name, description, thumbnail);
        validateNotBlank(name, description, thumbnail);
    }

    public Theme toTheme() {
        return new Theme(name, description, thumbnail);
    }
}
