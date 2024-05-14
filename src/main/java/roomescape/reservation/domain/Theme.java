package roomescape.reservation.domain;

public class Theme {

    private Long id;
    private final String themeName;
    private final String description;
    private final String thumbnail;

    public Theme(String themeName, String description, String thumbnail) {
        this.themeName = themeName;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public Theme(Long id, String themeName, String description, String thumbnail) {
        this.id = id;
        this.themeName = themeName;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return themeName;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnail() {
        return thumbnail;
    }
}
