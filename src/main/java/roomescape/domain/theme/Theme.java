package roomescape.domain.theme;

import jakarta.persistence.*;

@Entity
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

    public Theme(String name, String description, String thumbnail) {
        this.name = new ThemeName(name);
        this.description = new Description(description);
        this.thumbnail = new Thumbnail(thumbnail);
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
