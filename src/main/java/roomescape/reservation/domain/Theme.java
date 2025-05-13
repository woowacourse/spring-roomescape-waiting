package roomescape.reservation.domain;

import java.util.Objects;

public final class Theme {

    private final Long id;
    private final ThemeName name;
    private final ThemeDescription description;
    private final String thumbnail;

    public Theme(final Long id, final String name, final String description, final String thumbnail) {
        validateThumbnail(thumbnail);
        this.id = id;
        this.name = new ThemeName(name);
        this.description = new ThemeDescription(description);
        this.thumbnail = thumbnail;
    }

    private void validateThumbnail(final String thumbnail) {
        if (thumbnail == null) {
            throw new IllegalArgumentException("테마를 입력해야 합니다.");
        }
    }

    public boolean hasSameId(final long other) {
        return id == other;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name.getName();
    }

    public ThemeName getThemeName() {
        return name;
    }

    public String getDescription() {
        return description.getDescription();
    }

    public String getThumbnail() {
        return thumbnail;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Theme theme = (Theme) object;
        return Objects.equals(id, theme.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
