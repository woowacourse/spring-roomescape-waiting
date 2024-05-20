package roomescape.domain.theme;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class ThemeNameTest {

    @ParameterizedTest
    @DisplayName("이름이 공백이면 예외가 발생한다.")
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void validateName(String name) {
        assertThatThrownBy(() -> new ThemeName(name))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("테마명은 필수 값입니다.");
    }

    @Test
    @DisplayName("이름이 30자를 넘으면 예외가 발생한다.")
    void validateNameLength() {
        String name = "a".repeat(31);

        assertThatThrownBy(() -> new ThemeName(name))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("테마명은 30자를 넘을 수 없습니다.");
    }
}
