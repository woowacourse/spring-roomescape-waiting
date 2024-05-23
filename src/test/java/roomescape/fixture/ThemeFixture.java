package roomescape.fixture;

import roomescape.domain.reservation.Theme;

public enum ThemeFixture {

    TEST_THEME("test", "test", "test"),
    ;

    private final String name;
    private final String description;
    private final String thumbnail;

    ThemeFixture(String name, String description, String thumbnail) {
        this.name = name;
        this.description = description;
        this.thumbnail = thumbnail;
    }

    public Theme create() {
        return new Theme(name, description, thumbnail);
    }
}
