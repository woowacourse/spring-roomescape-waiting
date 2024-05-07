package roomescape.model.theme;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import roomescape.model.Reservation;
import roomescape.service.dto.ThemeDto;

import java.util.Objects;
import java.util.Set;

@Entity
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @NotNull
    @Embedded
    private Name name;
    @NotNull
    @Embedded
    private Description description;
    @NotNull
    @Embedded
    private Thumbnail thumbnail;
    @OneToMany(mappedBy = "theme")
    private Set<Reservation> reservations;

    public Theme(long id, String name, String description, String thumbnail) {
        this.id = id;
        this.name = new Name(name);
        this.description = new Description(description);
        this.thumbnail = new Thumbnail(thumbnail);
    }

    public Theme(String name, String description, String thumbnail) {
        this(0, name, description, thumbnail);
    }

    public Theme() {
    }

    public static Theme from(ThemeDto themeDto) {
        return new Theme(themeDto.getName().getName(), themeDto.getDescription(), themeDto.getThumbnail());
    }

    public long getId() {
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
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Theme theme = (Theme) o;
        return id == theme.id
                && Objects.equals(name.getName(), theme.name.getName())
                && Objects.equals(description.getDescription(), theme.description.getDescription())
                && Objects.equals(thumbnail.getThumbnail(), theme.thumbnail.getThumbnail());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, thumbnail);
    }
}
