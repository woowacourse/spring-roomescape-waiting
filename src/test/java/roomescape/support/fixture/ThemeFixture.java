package roomescape.support.fixture;

import org.springframework.boot.test.context.TestComponent;
import roomescape.domain.theme.Theme;

@TestComponent
public class ThemeFixture extends Fixture {

    public Theme save() {
        return save("테마명");
    }

    public Theme save(String name) {
        Theme theme = new Theme(name, "테마 설명", "https://example.com");
        em.persist(theme);
        synchronize();
        return theme;
    }
}
