package roomescape.service.fixture;

import roomescape.model.Theme;

public class TestThemeFactory {

    public static Theme createTheme(Long id, String name, String description, String thumbnail) {
        return new Theme(id, name, description, thumbnail);
    }

    public static Theme createTheme(Long id) {
        return createTheme(id, "name" + id, "description" + id, "thumbnail" + id);
    }
}
