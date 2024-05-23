package roomescape.support.fixture;

import roomescape.domain.theme.Theme;

public class ThemeFixture {

    public static Theme create() {
        return create("테마명");
    }

    public static Theme create(String name) {
        return new Theme(name, "테마 설명", "https://example.com");
    }
}
