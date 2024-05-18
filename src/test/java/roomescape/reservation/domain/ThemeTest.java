package roomescape.reservation.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.global.exception.ViolationException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.TestFixture.THEME_THUMBNAIL;
import static roomescape.TestFixture.WOOTECO_THEME_DESCRIPTION;

class ThemeTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = " ")
    @DisplayName("테마 이름은 비어있을 수 없다.")
    void validatePassword(String name) {
        // when & then
        assertThatThrownBy(() -> new Theme(name, WOOTECO_THEME_DESCRIPTION, THEME_THUMBNAIL))
                .isInstanceOf(ViolationException.class);
    }
}
