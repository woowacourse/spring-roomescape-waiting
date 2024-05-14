package roomescape.reservation.domain;

import java.util.Objects;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorType;

public class Theme {
    private final Long id;
    private String name;
    private String description;
    private String thumbnail;

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

    private void validate(String name, String description, String thumbnail) {
        validateString(name);
        validateString(description);
        validateString(thumbnail);
    }

    private void validateString(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new BusinessException(ErrorType.MISSING_REQUIRED_VALUE_ERROR);
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

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Theme theme = (Theme) o;
        return Objects.equals(getId(), theme.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return "Theme{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", thumbnail='" + thumbnail + '\'' +
                '}';
    }
}
