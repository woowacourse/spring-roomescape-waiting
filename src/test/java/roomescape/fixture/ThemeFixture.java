package roomescape.fixture;

import roomescape.reservation.domain.Theme;

public class ThemeFixture {
    public static Theme getTheme1() {
        return new Theme(1L, "theme1", "description", "thumbnail.png");
    }

    public static Theme getTheme2() {
        return new Theme(2L, "theme2", "description2", "thumbnail.png");
    }
}
