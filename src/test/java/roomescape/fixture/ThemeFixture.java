package roomescape.fixture;

import roomescape.domain.theme.domain.Theme;

public class ThemeFixture {
    public static final Theme DUMMY_THEME = new Theme(1L, "dummy", "dummy", "dummy");
    public static final Theme NULL_ID_DUMMY_THEME = new Theme(null, "dummy", "dummy", "dummy");
}
