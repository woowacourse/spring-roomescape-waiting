package roomescape.theme.fixture;

import roomescape.domain.Theme;
import roomescape.dto.request.ThemeRequestDto;

public class ThemeFixture {

    public static ThemeRequestDto createRequestDto(String name, String description, String thumbnail) {
        return new ThemeRequestDto(name, description, thumbnail);
    }

    public static Theme create(String name, String description, String thumbnail) {
        return new Theme(name, description, thumbnail);
    }
}
