package roomescape.theme.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import roomescape.theme.exception.InvalidThemeException;

@Entity
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    private String description;
    private String thumbnail;

    public Theme(final String name, final String description, final String thumbnail) {
        validateNameLength(name);
        validateDescriptionLength(description);
        validateThumbnailLength(thumbnail);
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public Theme() {
    }

    public Theme(final Long id, final String name, final String description, final String thumbnail) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public static Theme of(final String name, final String description, final String thumbnail) {
        return new Theme(name, description, thumbnail);
    }

    private void validateNameLength(final String value) {
        if (value.length() > 10) {
            throw new InvalidThemeException("이름은 10글자 이내여야 합니다.");
        }
    }

    private void validateDescriptionLength(final String value) {
        if (value.length() > 100) {
            throw new InvalidThemeException("설명은 100글자 이내여야 합니다.");
        }
    }

    private void validateThumbnailLength(final String value) {
        if (value.length() > 100) {
            throw new InvalidThemeException("썸네일은 100글자 이내여야 합니다.");
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
