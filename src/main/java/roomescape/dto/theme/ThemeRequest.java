package roomescape.dto.theme;

import java.util.Objects;
import roomescape.domain.Theme.Theme;

public record ThemeRequest(
        String name,
        String description,
        String thumbnail
) {

    public ThemeRequest {
        Objects.requireNonNull(name);
        Objects.requireNonNull(description);
        Objects.requireNonNull(thumbnail);
    }

    public Theme toEntity() {
        return new Theme(name, description, thumbnail);
    }
}
