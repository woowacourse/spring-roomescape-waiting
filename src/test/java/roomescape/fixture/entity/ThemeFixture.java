package roomescape.fixture.entity;

import roomescape.theme.domain.Theme;

public class ThemeFixture {

    public static final String THEME_NAME = "공포";
    public static final String THEME_DESCRIPTION = "공포 테마입니다.";
    public static final String THEME_THUMBNAIL = "horror.jpg";

    public static Theme create() {
        return Theme.create(THEME_NAME, THEME_DESCRIPTION, THEME_THUMBNAIL);
    }

    public static Theme create(String name, String description, String thumbnail) {
        return Theme.create(name, description, thumbnail);
    }
}
