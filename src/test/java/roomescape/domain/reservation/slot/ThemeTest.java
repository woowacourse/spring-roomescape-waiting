package roomescape.domain.reservation.slot;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ThemeTest {

    @DisplayName("Description이 존재하지 않으면 생성 불가능하다")
    @Test
    void invalidThemeDescriptionTest() {
        assertThatThrownBy(() ->
                        new Theme(1L, "가이온", null, "."))
                .isInstanceOf(NullPointerException.class);
    }

    @DisplayName("Thumbnail이 존재하지 않으면 생성 불가능하다")
    @Test
    void invalidThemeThumbnailTest() {
        assertThatThrownBy(() ->
                        new Theme(1L, "가이온", ".", null))
                .isInstanceOf(NullPointerException.class);
    }

    @DisplayName("공백이거나 이름이 존재하지 않는 경우 생성할 수 없다.")
    @ParameterizedTest
    @MethodSource("invalidNames")
    void invalidThemeNameTest(String themeName) {
        String description = ".";
        String thumbnail = ".";

        assertThatThrownBy(() -> new Theme(1L, themeName, description, thumbnail))
                .isInstanceOf(IllegalArgumentException.class);
    }

    static Stream<Arguments> invalidNames() {
        return Stream.of(
                Arguments.of(" "),
                Arguments.of(""),
                Arguments.of((String) null)
        );
    }
}
