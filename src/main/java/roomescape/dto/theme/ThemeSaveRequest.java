package roomescape.dto.theme;

import roomescape.domain.theme.Theme;

public record ThemeSaveRequest(String name, String description, String thumbnail) {

    public Theme toModel() {
        return new Theme(name, description, thumbnail);
    }
}
