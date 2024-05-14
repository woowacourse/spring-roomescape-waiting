package roomescape.theme.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import roomescape.global.exception.model.ValidateException;

import java.util.stream.Stream;

class ThemeTest {

    @ParameterizedTest
    @MethodSource("validateConstructorParameterBlankSource")
    @DisplayName("객체 생성 시, null 또는 공백이 존재하면 예외를 발생한다.")
    void validateConstructorParameterBlank(String name, String description, String thumbnail) {

        // when & then
        Assertions.assertThatThrownBy(() -> new Theme(name, description, thumbnail))
                .isInstanceOf(ValidateException.class);
    }

    static Stream<Arguments> validateConstructorParameterBlankSource() {
        return Stream.of(
                Arguments.of("테마명", "설명", " "),
                Arguments.of("테마명", " ", "썸네일URL"),
                Arguments.of("", "설명", "썸네일URL"),
                Arguments.of(null, null, null)
        );
    }
}
