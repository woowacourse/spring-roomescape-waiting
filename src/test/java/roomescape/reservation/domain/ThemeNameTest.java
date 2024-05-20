package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ThemeNameTest {

    @DisplayName("테마명에 공백 포함 한글, 영어, 숫자가 아닌 값을 입력시 예외가 발생한다.")
    @Test
    void createThemeNameByInvalidFormat() {
        assertThatThrownBy(() -> new ThemeName("공-포"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("테마명이 10글자를 초과하면 예외가 발생한다.")
    @Test
    void createThemeNameByLengthTest() {
        assertThatThrownBy(() -> new ThemeName("ABCDEFGHIJK"))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
