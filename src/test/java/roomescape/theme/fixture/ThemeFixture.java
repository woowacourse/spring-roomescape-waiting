package roomescape.theme.fixture;

import roomescape.theme.domain.Theme;
import roomescape.theme.domain.dto.ThemeRequestDto;

public class ThemeFixture {

    public static ThemeRequestDto createRequestDto(String name, String description, String thumbnail) {
        return new ThemeRequestDto(name, description, thumbnail);
    }

    public static Theme create(String name, String description, String thumbnail) {
        return new Theme(name, description, thumbnail);
    }
}
