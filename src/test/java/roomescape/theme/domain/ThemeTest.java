package roomescape.theme.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ThemeTest {

    private static Stream<Arguments> create_theme_exception_test() {
        return Stream.of(
                Arguments.of(null, "a", "a"),
                Arguments.of("", "a", "a"),
                Arguments.of("a", null, "a"),
                Arguments.of("a", "", "a"),
                Arguments.of("a", "a", null),
                Arguments.of("a", "a", ""),
                Arguments.of("a".repeat(256), "a", "a"),
                Arguments.of("a", "a".repeat(256), "a"),
                Arguments.of("a", "a", "a".repeat(256))
        );
    }

    @Test
    @DisplayName("아이디 할당 테스트")
    void assignId_Test() {
        // given
        Theme withoutId = Theme.createWithoutId("a", "b", "c");
        // when
        Theme theme = withoutId.assignId(1L);
        // then
        assertAll(
                () -> assertThat(theme.getId()).isEqualTo(1L),
                () -> assertThat(theme.getName()).isEqualTo("a"),
                () -> assertThat(theme.getDescription()).isEqualTo("b"),
                () -> assertThat(theme.getThumbnail()).isEqualTo("c")
        );
    }

    @ParameterizedTest
    @DisplayName("생성 검증 로직 테스트")
    @MethodSource
    void create_theme_exception_test(String name, String description, String thumbnail) {
        assertAll(
                () -> assertThatThrownBy(() -> Theme.createWithId(1L, name, description, thumbnail))
                        .isInstanceOf(IllegalArgumentException.class),
                () -> assertThatThrownBy(() -> Theme.createWithoutId(name, description, thumbnail))
                        .isInstanceOf(IllegalArgumentException.class)
        );
    }
}
