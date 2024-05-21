package roomescape.support.fixture;

import roomescape.domain.theme.Theme;

public class ThemeFixture {
    public static final Theme THEME = name("테마");

    public static Theme name(String name) {
        return new Theme(name, "테마 설명", "https://example.com");
    }
}
