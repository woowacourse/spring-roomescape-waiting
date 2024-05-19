package roomescape.presentation.acceptance;

import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeName;

class ThemeFixture {
    static Theme defaultValue() {
        return of("themeName", "description", "url");
    }

    static Theme of(String themeName, String description, String url) {
        return new Theme(new ThemeName(themeName), description, url);
    }
}
