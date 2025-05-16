package roomescape.test.fixture;

import roomescape.domain.Theme;
import roomescape.dto.business.ThemeCreationContent;

public class ThemeFixture {

    public static ThemeCreationContent createRequestDto(String name, String description, String thumbnail) {
        return new ThemeCreationContent(name, description, thumbnail);
    }

    public static Theme create(String name, String description, String thumbnail) {
        return Theme.createWithoutId(name, description, thumbnail);
    }
}
