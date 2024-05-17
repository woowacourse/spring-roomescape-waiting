package roomescape.support.fixture;

import roomescape.domain.theme.Theme;

public class ThemeFixture {

    public static Theme name(String name) {
        return new Theme(name, "테마 설명", "https://example.com");
    }
}
