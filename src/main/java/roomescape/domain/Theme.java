package roomescape.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.Objects;
import roomescape.exception.custom.InvalidDomainValueException;

@Entity
public class Theme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String description;

    private String thumbnailUrl;

    public Theme() {
    }

    public Theme(Long id, String name, String description, String thumbnailUrl) {
        validate(name, description, thumbnailUrl);
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnailUrl = thumbnailUrl;
    }

    public Theme(String name, String description, String thumbnailUrl) {
        this(null, name, description, thumbnailUrl);
    }

    public static Theme withId(Long id, Theme theme) {
        return new Theme(id, theme.name, theme.description, theme.thumbnailUrl);
    }

    private void validate(String name, String description, String thumbnailUrl) {
        if (name == null || name.isBlank()) {
            throw new InvalidDomainValueException("테마 이름은 비어 있을 수 없습니다.");
        }
        if (description == null || description.isBlank()) {
            throw new InvalidDomainValueException("테마 설명은 비어 있을 수 없습니다.");
        }
        if (thumbnailUrl == null || thumbnailUrl.isBlank()) {
            throw new InvalidDomainValueException("테마 썸네일은 비어 있을 수 없습니다.");
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

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Theme theme = (Theme) object;
        return Objects.equals(name, theme.name) && Objects.equals(description, theme.description)
                && Objects.equals(thumbnailUrl, theme.thumbnailUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, description, thumbnailUrl);
    }
}
