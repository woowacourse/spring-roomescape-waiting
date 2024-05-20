package roomescape.theme.domain;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.vo.Name;

class ThemeTest {

    @Test
    @DisplayName("전달 받은 데이터로 Theme 객체를 정상적으로 생성한다.")
    void constructTheme() {
        Theme theme = new Theme(1, new Name("미르"), "미르 방탈출", "썸네일 Url");

        assertAll(
                () -> assertEquals(1, theme.getId()),
                () -> assertEquals("미르", theme.getName()),
                () -> assertEquals("미르 방탈출", theme.getDescription()),
                () -> assertEquals("썸네일 Url", theme.getThumbnail())
        );
    }
}
