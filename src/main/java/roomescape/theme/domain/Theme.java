package roomescape.theme.domain;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private ThemeName name;

    @Embedded
    private ThemeDescription description;

    @Embedded
    private ThemeThumbnail thumbnail;

    public Theme(final Long id, final ThemeName name,
                 final ThemeDescription description, final ThemeThumbnail thumbnail
    ) {
        validateNotNull(name, description, thumbnail);
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public Theme(final Long id, final String name, final String description, final String thumbnail) {
        this(id, new ThemeName(name), new ThemeDescription(description), new ThemeThumbnail(thumbnail));
    }

    public Theme(final String name, final String description, final String thumbnail) {
        this(null, new ThemeName(name), new ThemeDescription(description), new ThemeThumbnail(thumbnail));
    }

    public Theme() {
    }

    private void validateNotNull(final ThemeName name, final ThemeDescription description, final ThemeThumbnail thumbnail) {
        if (name == null) {
            throw new IllegalArgumentException("테마 이름을 입력해야 합니다.");
        }
        if (description == null) {
            throw new IllegalArgumentException("테마 설명을 입력해야 합니다.");
        }
        if (thumbnail == null) {
            throw new IllegalArgumentException("테마 썸네일을 입력해야 합니다.");
        }
    }

    public Long getId() {
        return id;
    }

    public ThemeName getName() {
        return name;
    }

    public ThemeDescription getDescription() {
        return description;
    }

    public ThemeThumbnail getThumbnail() {
        return thumbnail;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final Theme theme = (Theme) o;
        return Objects.equals(id, theme.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
