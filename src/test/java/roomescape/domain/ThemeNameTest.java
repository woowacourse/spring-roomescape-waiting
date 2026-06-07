package roomescape.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThemeNameTest {

    @Test
    void ThemeName_객체_생성() {
        final String name = "우테코 탈출";

        final ThemeName themeName = new ThemeName(name);

        assertThat(themeName.name()).isEqualTo(name);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void 테마_이름이_null이거나_비어있으면_예외발생(final String name) {
        assertThatThrownBy(() -> new ThemeName(name))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
