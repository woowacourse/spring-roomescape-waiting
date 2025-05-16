package roomescape.test.fixture;

import roomescape.domain.Theme;
import roomescape.dto.request.ThemeCreationRequest;

public class ThemeFixture {

    public static ThemeCreationRequest createRequestDto(String name, String description, String thumbnail) {
        return new ThemeCreationRequest(name, description, thumbnail);
    }

    public static Theme create(String name, String description, String thumbnail) {
        return Theme.createWithoutId(name, description, thumbnail);
    }
}
