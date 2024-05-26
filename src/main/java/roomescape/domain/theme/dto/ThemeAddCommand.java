package roomescape.domain.theme.dto;

import roomescape.domain.theme.domain.Theme;

public record ThemeAddCommand(String name, String description, String thumbnail) {

    public static ThemeAddCommand from(ThemeAddRequest request) {
        return new ThemeAddCommand(request.name(), request.description(), request.thumbnail());
    }

    public Theme toEntity() {
        return new Theme(null, name, description, thumbnail);
    }
}
