package roomescape.domain.reservation;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Theme {
    private static final long NO_ID = 0;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "NAME"))
    private ThemeName name;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "DESCRIPTION"))
    private Description description;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "THUMBNAIL"))
    private Thumbnail thumbnail;

    public Theme() {
    }

    public Theme(long id, ThemeName name, Description description, Thumbnail thumbnail) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public Theme(String name, String description, String thumbnail) {
        this(NO_ID, new ThemeName(name), new Description(description), new Thumbnail(thumbnail));
    }

    public Theme(long id, Theme theme) {
        this(id, theme.name, theme.description, theme.thumbnail);
    }

    public Theme(ThemeName name, Description description, Thumbnail thumbnail) {
        this(NO_ID, name, description, thumbnail);
    }

    public long getId() {
        return id;
    }

    public ThemeName getName() {
        return name;
    }

    public Description getDescription() {
        return description;
    }

    public Thumbnail getThumbnail() {
        return thumbnail;
    }
}
