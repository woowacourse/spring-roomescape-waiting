package roomescape.unit.reservation.domain.theme;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import roomescape.reservation.domain.theme.ThemeThumbnail;

public class ThemeThumbnailTest {

    @DisplayName("테마 썸네일은 jpg, jpeg, png 형식이 아니면 예외가 발생한다.")
    @ParameterizedTest
    @MethodSource("invalidThumbnails")
    @NullAndEmptySource
    void validate(final String thumbnail) {
        // when & then
        assertThatThrownBy(() -> new ThemeThumbnail(thumbnail))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static Stream<Arguments> invalidThumbnails() {
        return Stream.of(
                Arguments.arguments(" "),
                Arguments.arguments("  "),
                Arguments.arguments("thumbnail.zip")
        );
    }
}
