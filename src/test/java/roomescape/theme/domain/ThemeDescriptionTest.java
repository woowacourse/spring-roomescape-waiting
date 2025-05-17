package roomescape.theme.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

class ThemeDescriptionTest {

    @DisplayName("테마 소개는 최소 5글자, 최대 200글자가 아니면 예외가 발생한다.")
    @ParameterizedTest
    @MethodSource
    @NullAndEmptySource
    void testValidateDescription(String description) {
        // when
        // then
        assertThatThrownBy(() -> new ThemeDescription(description))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("테마 소개는 최소 5글자, 최대 200글자여야합니다.");
    }

    private static Stream<Arguments> testValidateDescription() {
        return Stream.of(
                Arguments.arguments(" "),
                Arguments.arguments("aaaa"),
                Arguments.arguments("a".repeat(201))
        );
    }
}
