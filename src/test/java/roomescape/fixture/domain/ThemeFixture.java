package roomescape.fixture.domain;

import java.util.ArrayList;
import java.util.List;
import roomescape.theme.domain.Theme;

public class ThemeFixture {

    public static List<Theme> notSavedThemes(int count) {
        List<Theme> themes = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            themes.add(new Theme("자동 생성 테마" + i, "테마" + i + " 설명", "테마" + i + " 썸네일"));
        }
        return themes;
    }

    public static Theme notSavedTheme1() {
        return new Theme("테마1", "테마1 설명", "테마1 썸네일");
    }

    public static Theme notSavedTheme2() {
        return new Theme("테마2", "테마2 설명", "테마2 썸네일");
    }
}
