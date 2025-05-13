package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import roomescape.exception.theme.ThemeFieldRequiredException;

@Entity
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String thumbnail;

    public Theme() {
    }

    public Theme(Long id, String name, String description, String thumbnail) {
        validate(name, description, thumbnail);
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public Theme(String name, String description, String thumbnail) {
        this(null, name, description, thumbnail);
    }

    public Theme withId(Long id) {
        return new Theme(id, this.name, this.description, this.thumbnail);
    }

    private void validate(String name, String description, String thumbnail) {
        validateName(name);
        validateDescription(description);
        validateThumbnail(thumbnail);
    }

    private void validateThumbnail(String thumbnail) {
        if (thumbnail.isBlank()) {
            throw new ThemeFieldRequiredException("썸네일");
        }
    }

    private void validateDescription(String description) {
        if (description.isBlank()) {
            throw new ThemeFieldRequiredException("설명");
        }
    }

    private void validateName(String name) {
        if (name.isBlank()) {
            throw new ThemeFieldRequiredException("이름");
        }
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
}
