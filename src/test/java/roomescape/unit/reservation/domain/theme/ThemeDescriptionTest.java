package roomescape.unit.reservation.domain.theme;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import roomescape.reservation.domain.theme.ThemeDescription;

class ThemeDescriptionTest {

    @DisplayName("테마 설명은 최소 2글자, 최대 225글자가 아니면 예외가 발생한다.")
    @ParameterizedTest
    @MethodSource("invalidDescriptions")
    @NullAndEmptySource
    void validate(final String description) {
        // when & then
        assertThatThrownBy(() -> new ThemeDescription(description))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static Stream<Arguments> invalidDescriptions() {
        return Stream.of(
                Arguments.arguments(" "),
                Arguments.arguments("  "),
                Arguments.arguments("a".repeat(1)),
                Arguments.arguments("a".repeat(226))
        );
    }
}
