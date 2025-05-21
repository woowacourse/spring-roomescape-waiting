package roomescape.dto.theme;

import roomescape.domain.Theme;

public record ThemeCreateRequest(String name, String description, String thumbnail) {

    public Theme createWithoutId() {
        return Theme.createWithoutId(name, description, thumbnail);
    }
}
