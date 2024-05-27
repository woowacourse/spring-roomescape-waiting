package roomescape.fixture;

import roomescape.domain.reservation.Theme;

public enum ThemeFixture {

    TEST_THEME("test", "test", "test.com"),
    SCHOOL_THEME("school", "school theme", "school.com"),
    SPACE_THEME("space", "space theme", "space.com"),
    SPOOKY_THEME("spooky", "spooky theme", "spooky.com"),
    FANTASY_THEME("fantasy", "fantasy theme", "fantasy.com")
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
