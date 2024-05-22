package roomescape.theme.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.global.exception.IllegalRequestException;

class ThemeNameTest {

    @DisplayName("테마 이름의 길이가 범위를 넘어가는 경우 생성 시 예외가 발생한다")
    @ParameterizedTest
    @ValueSource(strings = {"", "LENGTH_OVER_TWENTY_STRING"})
    void should_throw_exception_when_theme_name_length_is_invalid(String name) {
        assertThatThrownBy(() -> new ThemeName(name))
                .isInstanceOf(IllegalRequestException.class);
    }

    @DisplayName("테마 이름이 Null인 경우 생성 시 예외가 발생한다")
    @Test
    void should_throw_exception_when_theme_name_is_null() {
        assertThatThrownBy(() -> new ThemeName(null))
                .isInstanceOf(IllegalRequestException.class);
    }

    @DisplayName("테마 이름이 공백 문자로만 이루어진 경우 생성 시 예외가 발생한다")
    @ParameterizedTest
    @ValueSource(strings = {" ", "    ", "      "})
    void should_throw_exception_when_theme_name_is_blank(String name) {
        assertThatThrownBy(() -> new ThemeName(name))
                .isInstanceOf(IllegalRequestException.class);
    }

    @DisplayName("규칙에 맞는 테마 이름 생성 시 예외가 발생하지 않는다")
    @Test
    void should_not_throw_exception_when_valid_theme_name_creation() {
        assertThatCode(() -> new ThemeName("올바른 테마 이름"))
                .doesNotThrowAnyException();
    }
}
