package roomescape.service.dto.theme;

import roomescape.domain.reservation.Theme;

public class ThemeResponse {

    private final long id;
    private final String name;
    private final String description;
    private final String thumbnail;

    public ThemeResponse(long id, String name, String description, String thumbnail) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public ThemeResponse(Theme theme) {
        this(theme.getId(), theme.getName(), theme.getDescription(), theme.getThumbnail());
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

    public String getThumbnail() {
        return thumbnail;
    }
}
