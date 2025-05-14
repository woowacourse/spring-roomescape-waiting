package roomescape.dto.theme;

import roomescape.domain.Theme;

public record ThemeResponseDto(long id, String name, String description, String thumbnail) {

    public static ThemeResponseDto from(Theme theme) {
        return new ThemeResponseDto(theme.id(), theme.name(), theme.description(), theme.thumbnail());
    }
}
