package roomescape.reservation.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String themeName;
    private String description;
    private String thumbnail;

    public Theme() {
    }

    public Theme(String themeName, String description, String thumbnail) {
        this.themeName = themeName;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public Theme(Long id, String themeName, String description, String thumbnail) {
        this.id = id;
        this.themeName = themeName;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return themeName;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnail() {
        return thumbnail;
    }
}
