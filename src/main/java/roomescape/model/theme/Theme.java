package roomescape.model.theme;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import roomescape.model.Reservation;
import roomescape.service.dto.ThemeDto;

import java.util.Objects;
import java.util.Set;

@Entity
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @Valid
    @Embedded
    private Name name;
    @NotNull
    @Valid
    @Embedded
    private Description description;
    @NotNull
    @Valid
    @Embedded
    private Thumbnail thumbnail;

    private Theme(Long id, Name name, Description description, Thumbnail thumbnail) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public Theme(Name name, Description description, Thumbnail thumbnail) {
        this(null, name, description, thumbnail);
    }

    public Theme(String name, String description, String thumbnail) {
        this(null, new Name(name), new Description(description), new Thumbnail(thumbnail));
    }

    public Theme(Long id, String name, String description, String thumbnail) { // TODO: for test?
        this(id, new Name(name), new Description(description), new Thumbnail(thumbnail));
    }

    protected Theme() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name.getName();
    }

    public String getDescription() {
        return description.getDescription();
    }

    public String getThumbnail() {
        return thumbnail.getThumbnail();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Theme theme = (Theme) o;
        return id.equals(theme.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, thumbnail);
    }
}
