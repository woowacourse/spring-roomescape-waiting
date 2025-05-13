package roomescape.theme.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.validate.InvalidInputException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class ThemeDescriptionTest {

    @Test
    @DisplayName("테마 설명이 null이면 예외가 발생한다")
    void validateNullThemeDescription() {
        // when
        // then
        assertThatThrownBy(() -> ThemeDescription.from(null))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Validation failed [while checking blank]: ThemeDescription.value");
    }

    @Test
    @DisplayName("테마 설명이 빈 문자열이면 예외가 발생한다")
    void validateBlankThemeDescription() {
        // when
        // then
        assertAll(() -> {
            assertThatThrownBy(() -> ThemeDescription.from(""))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("Validation failed [while checking blank]: ThemeDescription.value");

            assertThatThrownBy(() -> ThemeDescription.from(" "))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("Validation failed [while checking blank]: ThemeDescription.value");
        });
    }

    @Test
    @DisplayName("유효한 테마 설명으로 ThemeDescription 객체를 생성할 수 있다")
    void createValidThemeDescription() {
        // when
        final ThemeDescription themeDescription = ThemeDescription.from("테마 설명입니다.");

        // then
        assertAll(() -> {
            assertThat(themeDescription).isNotNull();
            assertThat(themeDescription.getValue()).isEqualTo("테마 설명입니다.");
        });
    }
}
