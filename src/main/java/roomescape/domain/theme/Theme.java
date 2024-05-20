package roomescape.domain.theme;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "theme")
public class Theme {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "NAME"))
    private ThemeName name;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "DESCRIPTION"))
    private Description description;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "THUMBNAIL"))
    private Thumbnail thumbnail;

    protected Theme() {
    }

    public Theme(ThemeName name, Description description, Thumbnail thumbnail) {
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public Theme(String name, String description, String thumbnail) {
        this(new ThemeName(name), new Description(description), new Thumbnail(thumbnail));
    }

    public Long getId() {
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
