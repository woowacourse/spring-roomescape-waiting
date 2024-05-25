package roomescape.fixture;

import roomescape.theme.domain.Theme;
import roomescape.theme.dto.ThemeCreateRequest;

public class ThemeFixture {
    public static final Theme THEME_1 = new Theme("레벨2 탈출", "우테코 레벨2 탈출기!", "https://img.jpg");
    public static final Theme THEME_2 = new Theme("레벨3 탈출", "우테코 레벨3 탈출기!", "https://img.jpg");
    public static final Theme THEME_3 = new Theme("레벨4 탈출", "우테코 레벨4 탈출기!", "https://img.jpg");

    public static ThemeCreateRequest toThemeCreateRequest(Theme theme) {
        return new ThemeCreateRequest(theme.getName(), theme.getDescription(), theme.getThumbnail());
    }
}
