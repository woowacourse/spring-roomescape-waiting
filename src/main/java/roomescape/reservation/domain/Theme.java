package roomescape.reservation.domain;

public class Theme {

    private Long id;
    private final ThemeName themeName;
    private final Description description;
    private final String thumbnail;

    public Theme(ThemeName themeName, Description description, String thumbnail) {
        this.themeName = themeName;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public Theme(Long id, ThemeName themeName, Description description, String thumbnail) {
        this.id = id;
        this.themeName = themeName;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return themeName.getName();
    }

    public String getDescription() {
        return description.getText();
    }

    public String getThumbnail() {
        return thumbnail;
    }
}
