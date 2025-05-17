package roomescape.theme.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class ThemeNameTest {

    @DisplayName("테마 이름은 최소 1글자, 최대 20글자가 아니면 예외가 발생한다.")
    @ParameterizedTest
    @ValueSource(strings = {" ", "   ", "123456789012345678901"})
    @NullAndEmptySource
    void testValidateName(String name) {
        // when
        // then
        assertThatThrownBy(() -> new ThemeName(name))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("테마 이름은 최소 1글자, 최대 20글자여야합니다.");
    }
}
