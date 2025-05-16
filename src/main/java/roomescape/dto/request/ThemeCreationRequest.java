package roomescape.dto.request;

import roomescape.domain.Theme;

public record ThemeCreationRequest(String name, String description, String thumbnail) {

    public Theme toEntity() {
        return Theme.createWithoutId(name, description, thumbnail);
    }
}
