package roomescape.domain;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThemeTest {

    @DisplayName("Description이 존재하지 않으면 생성 불가능하다")
    @Test
    void invalidThemeDescriptionTest() {
        assertThatThrownBy(() -> new Theme(1L, "가이온", null, "."))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("Thumbnail이 존재하지 않으면 생성 불가능하다")
    @Test
    void invalidThemeThumbnailTest1() {
        assertThatThrownBy(() -> new Theme(1L, "가이온", ".", null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("Thumbnail이 https URL 형식이 아니면 생성 불가능하다")
    @Test
    void invalidThemeThumbnailTest2() {
        assertThatThrownBy(() -> new Theme(1L, "가이온", ".", "."))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("테마 이름이 공백이거나 존재하지 않는 경우 생성할 수 없다.")
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
