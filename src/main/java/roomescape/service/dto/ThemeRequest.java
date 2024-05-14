package roomescape.service.dto;

import roomescape.domain.Theme;

public class ThemeRequest {
    private final String name;
    private final String description;
    private final String thumbnail;

    public ThemeRequest(String name, String description, String thumbnail) {
        validate(name, description, thumbnail);
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    private void validate(String name, String description, String thumbnail) {
        if (name.isBlank() || description.isBlank() || thumbnail.isBlank()) {
            throw new IllegalArgumentException();
        }
    }

    public Theme toTheme() {
        return new Theme(name, description, thumbnail);
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnail() {
        return thumbnail;
    }
}
