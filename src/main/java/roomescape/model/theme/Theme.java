package roomescape.model.theme;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Set;
import roomescape.model.Reservation;
import roomescape.service.dto.ThemeDto;

@Entity
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @NotNull
    @Embedded
    @Valid
    private Name name;
    @NotNull
    @Embedded
    @Valid
    private Description description;
    @NotNull
    @Embedded
    @Valid
    private Thumbnail thumbnail;
    @OneToMany(mappedBy = "theme", fetch = FetchType.LAZY)
    private Set<Reservation> reservations;

    private Theme(long id, Name name, Description description, Thumbnail thumbnail) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public Theme(long id, String name, String description, String thumbnail) {
        this(id, new Name(name), new Description(description), new Thumbnail(thumbnail));
    }

    public Theme() {
    }

    public static Theme from(ThemeDto themeDto) {
        return new Theme(0, themeDto.getName(), themeDto.getDescription(), themeDto.getThumbnail());
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
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Theme theme = (Theme) o;
        return id == theme.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
