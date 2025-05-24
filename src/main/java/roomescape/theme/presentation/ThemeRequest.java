package roomescape.theme.presentation;

import roomescape.theme.domain.Theme;

public class ThemeRequest {

    private final String name;
    private final String description;
    private final String thumbnail;

    public ThemeRequest(String name, String description, String thumbnail) {
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public Theme toTheme() {
        return new Theme(
                name,
                description,
                thumbnail
        );
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
