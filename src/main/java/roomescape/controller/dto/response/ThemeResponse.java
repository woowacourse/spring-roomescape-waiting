package roomescape.controller.dto.response;

import roomescape.domain.theme.Theme;

public class ThemeResponse {
    private final long id;
    private final String name;
    private final String description;
    private final String thumbnailUrl;

    public ThemeResponse(long id, String name, String description, String thumbnailUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
    }

    public static ThemeResponse from(Theme theme) {
        return new ThemeResponse(theme.getId(), theme.getName().getValue(), theme.getDescription(),
                theme.getThumbnailUrl().getValue());
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
}
