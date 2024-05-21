package roomescape;

import org.springframework.boot.test.context.TestComponent;
import roomescape.domain.Theme;
import roomescape.domain.ThemeName;

@TestComponent
public class ThemeFixture {

    public static Theme defaultValue() {
        return of("themeName", "description", "url");
    }

    public static Theme of(String themeName, String description, String url) {
        return new Theme(new ThemeName(themeName), description, url);
    }
}
