package roomescape.domain;

import java.util.Objects;
import roomescape.domain.exception.DomainErrorCode;
import roomescape.domain.exception.DomainPreconditions;

public class Theme {

    private final Long id;
    private final String name;
    private final String description;
    private final String thumbnailUrl;

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

    public static Theme of(Long id, Theme theme) {
        return new Theme(id, theme.name, theme.description, theme.thumbnailUrl);
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

    private void validate(String name, String description, String thumbnailUrl) {
        DomainPreconditions.requireNonBlank(name, DomainErrorCode.INVALID_INPUT, "name");
        DomainPreconditions.requireNonBlank(description, DomainErrorCode.INVALID_INPUT, "description");
        DomainPreconditions.requireNonBlank(thumbnailUrl, DomainErrorCode.INVALID_INPUT, "thumbnailUrl");
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        Theme theme = (Theme) object;
        return Objects.equals(id, theme.id) && Objects.equals(name, theme.name)
                && Objects.equals(description, theme.description) && Objects.equals(thumbnailUrl,
                theme.thumbnailUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, thumbnailUrl);
    }
}
