package roomescape.fixture;

import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeDescription;
import roomescape.theme.domain.ThemeName;
import roomescape.theme.domain.ThemeThumbnail;

public class ThemeFixture {

    public static final Theme THEME = new Theme(
            1L,
            new ThemeName("테마명1"),
            new ThemeDescription("이것은 테마1 입니다."),
            new ThemeThumbnail("썸네일1")
    );
}
