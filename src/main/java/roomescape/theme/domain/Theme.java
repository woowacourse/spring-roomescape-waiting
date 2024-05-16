package roomescape.theme.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import roomescape.name.domain.Name;

@Entity
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Embedded
    private Name name;
    @Column(nullable = false)
    private String description;
    @Column(nullable = false)
    private String thumbnail;

    public Theme() {
    }

    private Theme(long id, Name name, String description, String thumbnail) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    private Theme(String name, String description, String thumbnail) {
        this(0, new Name(name), description, thumbnail);
    }

    public static Theme themeOf(long id, String name, String description, String thumbnail) {
        return new Theme(id, new Name(name), description, thumbnail);
    }

    public static Theme saveThemeOf(String name, String description, String thumbnail) {
        return new Theme(name, description, thumbnail);
    }

    public static Theme saveThemeFrom(long id) {
        return new Theme(id, null, null, null);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name.getName();
    }

    public String getDescription() {
        return description;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setIdOnSave(long id) {
        this.id = id;
    }

}
