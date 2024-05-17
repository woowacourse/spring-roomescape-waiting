package roomescape.core.dto.theme;

import roomescape.core.domain.Theme;

public class ThemeResponse {
    private final Long id;
    private final String name;
    private final String description;
    private final String thumbnail;

    public ThemeResponse(final Theme theme) {
        this(theme.getId(), theme);
    }

    public ThemeResponse(final Long id, final Theme theme) {
        this.id = id;
        this.name = theme.getName();
        this.description = theme.getDescription();
        this.thumbnail = theme.getThumbnail();
    }

    public Long getId() {
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
