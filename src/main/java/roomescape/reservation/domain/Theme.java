package roomescape.reservation.domain;

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
    @AttributeOverride(name = "value", column = @Column(name = "name"))
    @Column(nullable = false)
    private ThemeName themeName;

    @Embedded
    @AttributeOverride(name = "text", column = @Column(name = "description"))
    @Column(nullable = false)
    private Description description;

    @Column(nullable = false)
    private String thumbnail;

    protected Theme() {
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
        return themeName.getValue();
    }

    public String getDescription() {
        return description.getText();
    }

    public String getThumbnail() {
        return thumbnail;
    }
}
