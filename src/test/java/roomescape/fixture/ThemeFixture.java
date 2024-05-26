package roomescape.fixture;

import java.util.ArrayList;
import java.util.List;
import roomescape.reservation.model.Theme;

public class ThemeFixture {

    public static Theme getOne() {
        return new Theme("테마 이름", "테마 설명", "테마 포스터");
    }

    public static List<Theme> get(int count) {
        List<Theme> themes = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            themes.add(new Theme(
                    null,
                    String.valueOf(i),
                    "테마 설명",
                    "테마 포스터")
            );
        }

        return themes;
    }
}
