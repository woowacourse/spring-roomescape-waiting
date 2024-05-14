package roomescape.reservation.domain;

public class Theme {
    private final Long id;
    private final ThemeName themeName;
    private final String description;
    private final String thumbnail;

    public Theme(final Long id, final ThemeName themeName, final String description, final String thumbnail) {
        validateDescription(description);
        validateThumbnail(thumbnail);
        this.id = id;
        this.themeName = themeName;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    private void validateDescription(final String description) {
        if (description == null || description.length() == 0) {
            throw new IllegalArgumentException("[ERROR] 테마 설명을 입력해주세요.");
        }
    }

    private void validateThumbnail(final String thumbnail) {
        if (thumbnail == null || thumbnail.length() == 0) {
            throw new IllegalArgumentException("[ERROR] 테마 썸네일을 입력해주세요.");
        }
    }

    public Theme(final String themeName, final String description, final String thumbnail) {
        this(null, new ThemeName(themeName), description, thumbnail);
    }

    public Theme assignId(final long id) {
        return new Theme(id, themeName, description, thumbnail);
    }

    public Long getId() {
        return id;
    }

    public ThemeName getThemeName() {
        return themeName;
    }

    public String getThemeNameValue() {
        return themeName.getValue();
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnail() {
        return thumbnail;
    }
}
