package roomescape.theme.domain;

import java.util.concurrent.atomic.AtomicLong;

public class ThemeFixture {
    private static final AtomicLong identifier = new AtomicLong(1L);

    public static Theme create() {
        long id = identifier.getAndIncrement();
        return new Theme(
            id,
            "testTheme" + id,
            "testDescription" + id,
            "testImage" + id
        );
    }

    public static Theme createWithoutId() {
        long id = identifier.getAndIncrement();
        return new Theme(
            null,
            "testTheme" + id,
            "testDescription" + id,
            "testImage" + id
        );
    }
}
