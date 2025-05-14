package roomescape.domain.theme;

import jakarta.persistence.Entity;
import java.util.Objects;

@Entity
public class Theme {
    private final Long id;
    private final ThemeName name;
    private final ThemeDescription description;
    private final ThemeThumbnail thumbnail;

    public Theme(
            final Long id,
            final ThemeName name,
            final ThemeDescription description,
            final ThemeThumbnail thumbnail
    ) {
        this.id = Objects.requireNonNull(id, "id는 null일 수 없습니다.");
        this.name = Objects.requireNonNull(name, "name은 null일 수 없습니다.");
        this.description = Objects.requireNonNull(description, "description은 null일 수 없습니다.");
        this.thumbnail = Objects.requireNonNull(thumbnail, "thumbnail은 null일 수 없습니다.");
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
}
