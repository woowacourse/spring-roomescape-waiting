package roomescape.theme;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.theme.controller.exception.ThemeDescriptionNullException;
import roomescape.theme.controller.exception.ThemeNameNullException;
import roomescape.theme.controller.exception.ThemeThumbnailNullException;
import roomescape.theme.domain.Theme;

public class ThemeTest {

    @Test
    @DisplayName("이름은 공백이 될 수 없다")
    void nameNullTest() {
        assertThatThrownBy(() ->
                new Theme(1L, "", "d", "d"))
                .isInstanceOf(ThemeNameNullException.class)
                .hasMessage("[ERROR] 테마의 이름은 공백이 될 수 없습니다.");
    }

    @Test
    @DisplayName("썸네일은 공백이 될 수 없다.")
    void thumbnailNullTest() {
        assertThatThrownBy(() ->
                new Theme(1L, "d", "d", ""))
                .isInstanceOf(ThemeThumbnailNullException.class)
                .hasMessage("[ERROR] 테마의 썸네일은 공백이 될 수 없습니다.");
    }

    @Test
    @DisplayName("설명은 공백이 될 수 없다.")
    void descriptionNullTest() {
        assertThatThrownBy(() ->
                new Theme(1L, "d", "", "d"))
                .isInstanceOf(ThemeDescriptionNullException.class)
                .hasMessage("[ERROR] 테마의 설명은 공백이 될 수 없습니다.");
    }
}
