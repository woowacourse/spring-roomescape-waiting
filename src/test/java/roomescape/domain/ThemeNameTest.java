package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ThemeNameTest {

    @DisplayName("테마명은 20자 이하이다.")
    @Test
    void themeNameLength() {
        assertThatThrownBy(() -> new ThemeName("nameNameNameNameName1"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("테마명은 필수로 입력되어야 한다.")
    @ValueSource(strings = {"", " "})
    @ParameterizedTest
    void themeBlank(String value) {
        assertThatThrownBy(() -> new ThemeName(value))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
