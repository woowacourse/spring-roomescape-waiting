package roomescape.theme.domain.fixture;

import roomescape.theme.domain.Theme;

public final class ThemeFixture {

    private ThemeFixture() {
    }

    public static Theme createDefaultTheme() {
        return Theme.create("공포테마", "https://image.com/image.png", "어마무시한 공포 테마입니다.");
    }

    public static Theme createThemeWithId() {
        return Theme.restore(1L, "공포테마", "https://image.com/image.png", "어마무시한 공포 테마입니다.", true);
    }

    public static Theme createTheme(String name, String thumbnailImageUrl, String description) {
        return Theme.create(name, thumbnailImageUrl, description);
    }
}
