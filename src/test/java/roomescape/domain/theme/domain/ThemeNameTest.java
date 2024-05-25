package roomescape.domain.theme.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.domain.theme.domain.ThemeName.THEME_NAME_EMPTY_ERROR_MESSAGE;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.ValueNullOrEmptyException;

class ThemeNameTest {

    @DisplayName("테마 이름이 Null이면 예외가 발생합니다.")
    @Test
    void should_throw_exception_when_theme_name_is_null() {
        assertThatThrownBy(() -> new ThemeName(null))
                .isInstanceOf(ValueNullOrEmptyException.class)
                .hasMessage(THEME_NAME_EMPTY_ERROR_MESSAGE);
    }

    @DisplayName("테마 이름이 비어있으면 예외가 발생합니다.")
    @Test
    void should_throw_exception_when_theme_name_is_empty() {
        assertThatThrownBy(() -> new ThemeName(""))
                .isInstanceOf(ValueNullOrEmptyException.class)
                .hasMessage(THEME_NAME_EMPTY_ERROR_MESSAGE);
    }
}
