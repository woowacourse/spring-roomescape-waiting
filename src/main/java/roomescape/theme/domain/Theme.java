package roomescape.theme.domain;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
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
    @AttributeOverride(name = "value", column = @Column(name = "name", nullable = false))
    @JsonUnwrapped
    private ThemeName name;
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "description", nullable = false))
    @JsonUnwrapped
    private Description description;
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "thumbnail", nullable = false))
    @JsonUnwrapped
    private Thumbnail thumbnail;

    public Theme() {
    }

    public Theme(final String name, final String description, final String thumbnail) {
        this(null, name, description, thumbnail);
    }

    public Theme(final Long id, final Theme theme) {
        this(id, theme.name, theme.description, theme.thumbnail);
    }

    public Theme(final Long id, final String name, final String description, final String thumbnail) {
        this(id, new ThemeName(name), new Description(description), new Thumbnail(thumbnail));
    }

    public Theme(final Long id, final ThemeName name, final Description description, final Thumbnail thumbnail) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name.value();
    }

    public String getDescription() {
        return description.value();
    }

    public String getThumbnail() {
        return thumbnail.value();
    }

    @Override
    public String toString() {
        return "Theme{" +
                "id=" + id +
                ", name=" + name +
                ", description=" + description +
                ", thumbnail=" + thumbnail +
                '}';
    }
}
