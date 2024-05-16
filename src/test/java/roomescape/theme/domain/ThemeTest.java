package roomescape.theme.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.global.exception.model.ValidateException;

class ThemeTest {

    @ParameterizedTest
    @ValueSource(strings = {"", "aaaaaaaaaaaaaaaaaaaaa"})
    @DisplayName("테마의 이름이 1자 이상 20자 이하가 아니라면 예외를 발생시킨다.")
    void validateThemeNameLength(String name) {
        Assertions.assertThatThrownBy(() -> new Theme(name, "테마설명", "썸네일URL"))
                .isInstanceOf(ValidateException.class);
    }

    @Test
    @DisplayName("테마의 이름이 공백이면 예외를 발생시킨다.")
    void validateThemeNameBlank() {
        String name = "    ";

        Assertions.assertThatThrownBy(() -> new Theme(name, "테마설명", "썸네일URL"))
                .isInstanceOf(ValidateException.class);
    }

    @Test
    @DisplayName("테마의 설명이 1자 이상 100자 이하가 아니라면 예외를 발생시킨다.")
    void validateThemeDescriptionLength() {
        String description = "a".repeat(101);
        Assertions.assertThatThrownBy(() -> new Theme("테마명", description, "썸네일URL"))
                .isInstanceOf(ValidateException.class);
    }

    @Test
    @DisplayName("테마의 이름이 공백이면 예외를 발생시킨다.")
    void validateThemeDescriptionBlank() {
        String description = "    ";

        Assertions.assertThatThrownBy(() -> new Theme("테마명", description, "썸네일URL"))
                .isInstanceOf(ValidateException.class);
    }

    @Test
    @DisplayName("테마의 썸네일이 공백이면 예외를 발생시킨다.")
    void validateThemeThumbnailBlank() {
        String thumbnail = "    ";

        Assertions.assertThatThrownBy(() -> new Theme("테마명", "테마설명", thumbnail))
                .isInstanceOf(ValidateException.class);
    }
}
