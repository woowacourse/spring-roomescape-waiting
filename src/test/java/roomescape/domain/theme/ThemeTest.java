package roomescape.domain.theme;

import java.util.stream.Stream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ThemeTest {
    @ParameterizedTest
    @MethodSource("nullCases")
    void 매개변수에_NULL이_포함되면_예외가_발생한다(ThemeName themeName, String description, ThumbnailUrl thumbnailUrl) {
        Assertions.assertThatThrownBy(() -> Theme.create(themeName, description, thumbnailUrl))
                .isInstanceOf(NullPointerException.class);
    }

    static Stream<Arguments> nullCases() {
        ThemeName name = new ThemeName("공포의 방");
        String description = "무서운 테마";
        ThumbnailUrl thumbnailUrl = new ThumbnailUrl("https://zeze.com/thumb.jpg");

        return Stream.of(
                Arguments.of(null, description, thumbnailUrl),
                Arguments.of(name, null, thumbnailUrl),
                Arguments.of(name, description, null)
        );
    }
}
