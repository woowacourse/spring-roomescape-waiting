package roomescape.theme.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import java.util.Objects;
import java.util.regex.Pattern;
import roomescape.exception.BadRequestException;
import roomescape.exception.ExceptionCause;

@Entity
public class Theme {

    private static final String URL_REGEX = "^https://.*";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String description;

    @NotBlank
    private String thumbnail;

    protected Theme() {
    }

    public Theme(String name, String description, String thumbnail) {
        validateName(name);
        validateDescription(description);
        validateThumbnail(thumbnail);

        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public Theme(Long id, String name, String description, String thumbnail) {
        validateName(name);
        validateDescription(description);
        validateThumbnail(thumbnail);
        this.id = id;
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new BadRequestException(ExceptionCause.THEME_NAME_INVALID_INPUT);
        }
    }

    private void validateDescription(String description) {
        if (description == null) {
            throw new BadRequestException(ExceptionCause.THEME_DESCRIPTION_INVALID_INPUT);
        }
    }

    private void validateThumbnail(String thumbnail) {
        if (thumbnail == null) {
            throw new BadRequestException(ExceptionCause.THEME_THUMBNAIL_INVALID_INPUT);
        }
        if (!Pattern.matches(URL_REGEX, thumbnail)) {
            throw new BadRequestException(ExceptionCause.THEME_THUMBNAIL_INVALID_INPUT);
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
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Theme theme = (Theme) o;
        return Objects.equals(id, theme.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
