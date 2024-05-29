package roomescape.fixture;

import roomescape.entity.Theme;

public class ThemeFixture {
    public static final Theme DEFAULT_THEME = new Theme(1L, "name", "description", "thumbnail");

    public static final Theme themeOfName(String name) {
        return new Theme(name, "description", "thumbnail");
    }
}
