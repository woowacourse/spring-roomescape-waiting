package roomescape.fixture.domain;

import roomescape.theme.domain.Theme;

public class ThemeFixture {

    public static Theme NOT_SAVED_THEME_1() {
        return new Theme("테마1", "테마1 설명", "테마1 썸네일");
    }

    public static Theme NOT_SAVED_THEME_2() {
        return new Theme("테마2", "테마2 설명", "테마2 썸네일");
    }
}
