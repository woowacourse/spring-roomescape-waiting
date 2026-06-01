package roomescape.domain.fixture;

import roomescape.domain.Theme;

public class ThemeFixture {

    public static Theme createDefaultTheme() {
        return new Theme("공포테마", "어마무시한 공포 테마입니다.", "https://image.com/image.png");
    }

    public static Theme createThemeWithId() {
        return new Theme(1L, "공포테마", "어마무시한 공포 테마입니다.", "https://image.com/image.png", true);
    }

    public static Theme createdInactive() {
        return new Theme(1L, "공포테마", "어마무시한 공포 테마입니다.", "https://image.com/image.png", false);
    }
}
