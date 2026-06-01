package roomescape.domain.slot.theme;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static roomescape.domain.slot.theme.ThemeName.THEME_NAME_MAX_LENGTH;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ThemeNameTest {

    @Test
    @DisplayName("올바른 정보로 테마 이름을 생성하면 성공한다.")
    void 테마_이름_생성_테스트() {
        String themeName = "우테코";

        assertDoesNotThrow(() -> new ThemeName(themeName));
    }

    @Test
    @DisplayName("테마 이름이 빈칸 이면 예외가 발생한다.")
    void 테마_이름_빈칸_예외_테스트() {
        String themeName = "";

        assertThatThrownBy(() -> new ThemeName(themeName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("테마 이름은 비어 있을 수 없습니다.");
    }

    @Test
    @DisplayName("테마 이름 글자 수 제한을 초과하면 예외가 발생한다.")
    void 테마_이름_글자_수_초과_예외_테스트() {
        String themeName = "0".repeat(THEME_NAME_MAX_LENGTH + 1);

        assertThatThrownBy(() -> new ThemeName(themeName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("테마 이름은 %d자를 초과할 수 없습니다.".formatted(THEME_NAME_MAX_LENGTH));
    }
}
