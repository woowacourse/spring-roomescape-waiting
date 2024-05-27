package roomescape.reservation.model;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

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

    protected Theme() {
    }

    public Theme(
            final String name,
            final String description,
            final String thumbnail
    ) {
        this(
                null,
                new ThemeName(name),
                new ThemeDescription(description),
                new ThemeThumbnail(thumbnail)
        );
    }

    public Theme (
            final Long id,
            final String name,
            final String description,
            final String thumbnail
    ) {
        this(
                id,
                new ThemeName(name),
                new ThemeDescription(description),
                new ThemeThumbnail(thumbnail)
        );
    }

    private Theme(
            final Long id,
            final ThemeName name,
            final ThemeDescription description,
            final ThemeThumbnail thumbnail
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
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
