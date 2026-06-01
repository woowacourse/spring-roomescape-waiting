package roomescape.domain.theme;

import java.util.Objects;

public class Theme {
    private final Long id;
    private final String themeName;
    private final String thumbnailUrl;
    private final String description;

    private Theme(Long id, String themeName, String thumbnailUrl, String description) {
        Objects.requireNonNull(themeName, "테마 이름은 필수입니다.");

        this.id = id;
        this.themeName = themeName;
        this.thumbnailUrl = thumbnailUrl;
        this.description = description;
    }

    public static Theme from(Long id, String name, String thumbnailUrl, String description) {
        Objects.requireNonNull(id, "조회 및 복원시 Theme의 id는 필수입니다.");

        return new Theme(id, name, thumbnailUrl, description);
    }

    public static Theme create(String name, String thumbnailUrl, String description) {
        return new Theme(null, name, thumbnailUrl, description);
    }

    public Long getId() {
        return id;
    }

    public String getThemeName() {
        return themeName;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Theme theme = (Theme) o;
        return Objects.equals(id, theme.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
