package roomescape.theme.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.validate.InvalidInputException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class ThemeNameTest {

    @Test
    @DisplayName("테마 이름이 null이면 예외가 발생한다")
    void validateNullThemeName() {
        // when
        // then
        assertThatThrownBy(() -> ThemeName.from(null))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Validation failed [while checking blank]: ThemeName.value");
    }

    @Test
    @DisplayName("테마 이름이 빈 문자열이면 예외가 발생한다")
    void validateBlankThemeName() {
        // when
        // then
        assertAll(() -> {
            assertThatThrownBy(() -> ThemeName.from(""))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("Validation failed [while checking blank]: ThemeName.value");

            assertThatThrownBy(() -> ThemeName.from(" "))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("Validation failed [while checking blank]: ThemeName.value");
        });
    }

    @Test
    @DisplayName("유효한 테마 이름으로 ThemeName 객체를 생성할 수 있다")
    void createValidThemeName() {
        // when
        ThemeName themeName = ThemeName.from("방탈출 테마");

        // then
        assertAll(() -> {
            assertThat(themeName).isNotNull();
            assertThat(themeName.getValue()).isEqualTo("방탈출 테마");
        });
    }
}
