package roomescape.fixture;

import roomescape.domain.Theme;
import roomescape.domain.Thumbnail;

public class ThemeFixtures {

    private ThemeFixtures() {
    }

    public static Theme createDefaultTheme() {
        return new Theme(null, "default", "default", new Thumbnail("https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"));
    }
}
