package roomescape.theme.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ThemeTest {

    @DisplayName("테마 아이디와 테마 이름이 같은 경우 동일하다")
    @Test
    void isEqual() {
        Theme theme1 = new Theme(1L, "theme1", "description1", "thumbnail1");
        Theme theme2 = new Theme(1L, "theme1", "description2", "thumbnail2");

        assertThat(theme1.equals(theme2)).isTrue();
    }
}
