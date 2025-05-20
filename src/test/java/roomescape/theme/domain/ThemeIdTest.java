package roomescape.theme.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.validate.InvalidArgumentException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class ThemeIdTest {

    @Test
    @DisplayName("테마 ID가 null이면 예외가 발생한다")
    void validateNullThemeId() {
        // when
        // then
        assertThatThrownBy(() -> ThemeId.from(null))
                .isInstanceOf(InvalidArgumentException.class)
                .hasMessage("Validation failed [while checking null]: EntityId.value");
    }

    @Test
    @DisplayName("유효한 ID로 ThemeId 객체를 생성할 수 있다")
    void createValidThemeId() {
        // given
        final Long id = 1L;

        // when
        final ThemeId themeId = ThemeId.from(id);

        // then
        assertAll(() -> {
            assertThat(themeId).isNotNull();
            assertThat(themeId.getValue()).isEqualTo(id);
        });
    }
} 
