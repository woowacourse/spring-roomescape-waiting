package roomescape.reservation.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private ThemeName themeName;

    @Embedded
    @AttributeOverride(name = "text", column = @Column(name = "description"))
    private Description description;

    private String thumbnail;

    @OneToMany(mappedBy = "theme")
    private Set<Reservation> reservations = new HashSet<>();

    public Theme() {
    }

    public Theme(ThemeName themeName, Description description, String thumbnail) {
        this.themeName = themeName;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public Theme(Long id, ThemeName themeName, Description description, String thumbnail) {
        this.id = id;
        this.themeName = themeName;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return themeName.getName();
    }

    public String getDescription() {
        return description.getText();
    }

    public String getThumbnail() {
        return thumbnail;
    }
}
