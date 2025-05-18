package roomescape.unit.reservation.domain.theme;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import roomescape.reservation.domain.theme.ThemeName;

class ThemeNameTest {

    @DisplayName("테마 이름은 최소 2글자, 최대 20글자가 아니면 예외가 발생한다.")
    @ParameterizedTest
    @MethodSource("invalidNames")
    @NullAndEmptySource
    void validate(final String name) {
        // when & then
        assertThatThrownBy(() -> new ThemeName(name))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static Stream<Arguments> invalidNames() {
        return Stream.of(
                Arguments.arguments(" "),
                Arguments.arguments("  "),
                Arguments.arguments("a".repeat(1)),
                Arguments.arguments("a".repeat(21))
        );
    }
}
