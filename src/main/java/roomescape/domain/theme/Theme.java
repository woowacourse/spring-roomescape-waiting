package roomescape.domain.theme;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Objects;

@Entity
public class Theme {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Embedded
    private ThemeName name;
    @Column(nullable = false)
    private String description;
    @Embedded
    private ThumbnailUrl thumbnailUrl;

    protected Theme() {
    }

    private Theme(Long id, ThemeName name, String description, ThumbnailUrl thumbnailUrl) {
        this.id = id;
        this.name = Objects.requireNonNull(name);
        this.description = Objects.requireNonNull(description);
        this.thumbnailUrl = Objects.requireNonNull(thumbnailUrl);
    }

    public static Theme load(Long id, ThemeName name, String description, ThumbnailUrl thumbnailUrl) {
        return new Theme(id, name, description, thumbnailUrl);
    }

    public static Theme create(ThemeName name, String description, ThumbnailUrl thumbnailUrl) {
        return new Theme(null, name, description, thumbnailUrl);
    }

    public Theme withId(long id) {
        return new Theme(id, name, description, thumbnailUrl);
    }

    public long getId() {
        return id;
    }

    public ThemeName getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public ThumbnailUrl getThumbnailUrl() {
        return thumbnailUrl;
    }
}
