package roomescape.fixture;

import roomescape.domain.Theme;
import roomescape.dto.ThemeRequest;
import roomescape.dto.ThemeResponse;

public class ThemeFixture {
    public static final Theme DEFAULT_THEME = new Theme(1L, "theme", "description", "https://thumbnail.com");
    public static final ThemeRequest DEFAULT_REQUEST = new ThemeRequest(DEFAULT_THEME.getName(),
            DEFAULT_THEME.getDescription(), DEFAULT_THEME.getThumbnail());
    public static final ThemeResponse DEFAULT_RESPONSE = new ThemeResponse(DEFAULT_THEME.getId(),
            DEFAULT_THEME.getName(), DEFAULT_THEME.getDescription(), DEFAULT_THEME.getThumbnail());


    public static Theme from(String name) {
        return new Theme(name, "description", "https://thumbnail.com");
    }
}
