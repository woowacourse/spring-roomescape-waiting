package roomescape.dto.request;

import roomescape.domain.Theme;

public record ThemeRequestDto(String name, String description, String thumbnail) {

    public Theme toEntity() {
        return new Theme(name, description, thumbnail);
    }
}
