package roomescape.theme.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.global.exception.DomainValidationException;

class DescriptionTest {

    @DisplayName("테마 설명이 널인 경우 생성 시 예외가 발생한다")
    @Test
    void should_throw_exception_when_theme_description_is_null() {
        assertThatThrownBy(() -> new Description(null))
                .isInstanceOf(DomainValidationException.class);
    }

    @DisplayName("테마 설명이 공백 문자로만 이루어져 있는 경우 예외가 발생한다")
    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "        "})
    void should_throw_exception_when_theme_description_is_blank(String description) {
        assertThatThrownBy(() -> new Description(description))
                .isInstanceOf(DomainValidationException.class);
    }

    @DisplayName("올바른 테마 설명 생성 시 예외가 발생하지 않는다")
    @Test
    void should_not_throw_exception_when_valid_description_creation() {
        assertThatCode(() -> new Description("올바른 테마 설명"))
                .doesNotThrowAnyException();
    }
}
