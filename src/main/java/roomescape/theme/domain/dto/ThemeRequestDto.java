package roomescape.theme.domain.dto;

import roomescape.theme.domain.Theme;

public record ThemeRequestDto(String name, String description, String thumbnail) {

    public Theme toEntity() {
        return new Theme(name, description, thumbnail);
    }
}
