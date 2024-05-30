package roomescape.service.dto.request;

import java.util.stream.Stream;
import roomescape.domain.Theme;

public record ThemeRequest(String name, String description, String thumbnail) {
    public ThemeRequest {
        validate(name, description, thumbnail);
    }

    private void validate(String... values) {
        if (Stream.of(values).anyMatch(String::isBlank)) {
            throw new IllegalArgumentException();
        }
    }

    public Theme toTheme() {
        return new Theme(name, description, thumbnail);
    }
}
