package roomescape.theme.domain;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ThemeTest {

    @Test
    @DisplayName("전달 받은 데이터로 Theme 객체를 정상적으로 생성한다.")
    void constructTheme() {
        Theme theme = Theme.themeOf(1, "미르", "미르 방탈출", "썸네일 Url");

        assertAll(
                () -> assertEquals(theme.getId(), 1),
                () -> assertEquals(theme.getName(), "미르"),
                () -> assertEquals(theme.getDescription(), "미르 방탈출"),
                () -> assertEquals(theme.getThumbnail(), "썸네일 Url")
        );
    }
}
