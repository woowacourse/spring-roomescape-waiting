package roomescape.controller.response;

import roomescape.model.Theme;

public class ReservationThemeResponse {

    private Long id;
    private String name;
    private String description;
    private String thumbnail;

    public ReservationThemeResponse() {
    }

    public ReservationThemeResponse(Long id, String name, String description, String thumbnail) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public static ReservationThemeResponse of(Theme domain) {
        return new ReservationThemeResponse(domain.getId(), domain.getName(), domain.getDescription(), domain.getThumbnail());
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
