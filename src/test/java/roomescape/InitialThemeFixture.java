package roomescape;

import roomescape.theme.domain.Name;
import roomescape.theme.domain.Theme;

public class InitialThemeFixture {

    public static final int INITIAL_THEME_COUNT = 4;
    public static final Theme THEME_1 = new Theme(
            1L,
            new Name("레벨1 탈출"),
            "우테코 레벨1를 탈출하는 내용입니다.",
            "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
    );
    public static final Theme THEME_2 = new Theme(
            2L,
            new Name("레벨2 탈출"),
            "우테코 레벨2를 탈출하는 내용입니다.",
            "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
    );
    public static final Theme THEME_3 = new Theme(
            3L,
            new Name("레벨3 탈출"),
            "우테코 레벨3를 탈출하는 내용입니다.",
            "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
    );
    public static final Theme NOT_RESERVED_THEME = new Theme(
            4L,
            new Name("레벨4 탈출"),
            "우테코 레벨4를 탈출하는 내용입니다.",
            "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
    );
    public static final Theme NOT_SAVED_THEME = new Theme(
            null,
            new Name("not saved theme name"),
            "any description",
            "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg"
    );
}
