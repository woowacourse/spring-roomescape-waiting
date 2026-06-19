package roomescape.theme.fixture;

import roomescape.theme.domain.Theme;

public class ThemeFixture {

    private static final Long DEFAULT_AMOUNT = 1000L;

    public static Theme theme() {
        return Theme.create("테마1", "설명1", "썸네일1", DEFAULT_AMOUNT);
    }

    public static Theme theme(String name) {
        return Theme.create(name, "설명", "썸네일", DEFAULT_AMOUNT);
    }

    public static Theme activeTheme() {
        Theme theme = Theme.create("테마1", "설명1", "썸네일1", DEFAULT_AMOUNT);
        theme.updateStatus(true);
        return theme;
    }

    public static Theme activeTheme(String name) {
        Theme theme = Theme.create(name, "설명1", "썸네일1", DEFAULT_AMOUNT);
        theme.updateStatus(true);
        return theme;
    }

    public static Theme inActiveTheme() {
        Theme theme = Theme.create("테마1", "설명1", "썸네일1", DEFAULT_AMOUNT);
        theme.updateStatus(false);
        return theme;
    }

}
