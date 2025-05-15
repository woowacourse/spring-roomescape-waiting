package roomescape.presentation.dto;

import roomescape.business.domain.Theme;

public record ThemeRequest(String name, String description, String thumbnail) {

    public Theme toDomain() {
        return new Theme(name, description, thumbnail);
    }
}
