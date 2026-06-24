package roomescape.fixture;

import roomescape.domain.Theme;

public class ThemeFixture {

    private ThemeFixture() {
    }

    public static Theme create() {
        return new Theme(
                1L,
                "공포",
                "무서운 테마",
                "https://example.com/horror.jpg"
        );
    }
}
