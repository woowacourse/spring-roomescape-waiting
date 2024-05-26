package roomescape.service.dto.request;

import roomescape.domain.Theme;

public record ThemeRequest(String name, String description, String thumbnail) {
    public ThemeRequest {
        validate(name, description, thumbnail);
    }

    private void validate(String name, String description, String thumbnail) {
        if (name.isBlank() || description.isBlank() || thumbnail.isBlank()) {
            throw new IllegalArgumentException();
        }
    }

    public Theme toTheme() {
        return new Theme(name, description, thumbnail);
    }
}
