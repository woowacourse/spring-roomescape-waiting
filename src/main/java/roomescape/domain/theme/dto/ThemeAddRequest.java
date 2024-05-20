package roomescape.domain.theme.dto;

import roomescape.domain.theme.domain.Theme;

public record ThemeAddRequest(String name, String description, String thumbnail) {

    public Theme toEntity() {
        return new Theme(null, name, description, thumbnail);
    }
}
