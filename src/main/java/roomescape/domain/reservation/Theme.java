package roomescape.domain.reservation;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String description;
    @Column(nullable = false)
    private String thumbnail;

    public Theme(String name, String description, String thumbnail) {
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    protected Theme() {
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || !(o instanceof Theme)) {
            return false;
        }
        Theme theme = (Theme) o;
        return Objects.equals(this.getId(), theme.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
