package roomescape.reservation.presentation.dto;

import roomescape.reservation.domain.Theme;

public class ThemeResponse {
    private Long id;
    private String name;
    private String description;
    private String thumbnail;

    private ThemeResponse() {
    }

    public ThemeResponse(Theme theme) {
        this.id = theme.getId();
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
