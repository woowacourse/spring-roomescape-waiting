package roomescape.presentation.acceptance;

import roomescape.domain.Theme;
import roomescape.domain.ThemeName;

class ThemeFixture {

    static Theme defaultValue() {
        return of("themeName", "description", "url");
    }

    static Theme of(String themeName, String description, String url) {
        return new Theme(new ThemeName(themeName), description, url);
    }
}
