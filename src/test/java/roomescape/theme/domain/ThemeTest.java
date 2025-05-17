package roomescape.theme.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.validate.InvalidInputException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

class ThemeTest {

    @Test
    @DisplayName("테마 필드가 null이 될 수 없다")
    void validateNull() {
        // given
        final ThemeName name = ThemeName.from("테마 이름");
        final ThemeDescription description = ThemeDescription.from("테마 설명");
        final ThemeThumbnail thumbnail = ThemeThumbnail.from("https://example.com/image.jpg");
        final ThemeId id = ThemeId.from(1L);

        // when
        // then
        assertAll(
                () -> assertThatThrownBy(() -> Theme.withId(null, name, description, thumbnail))
                        .isInstanceOf(NullPointerException.class),

                () -> assertThatThrownBy(() -> Theme.withoutId(null, description, thumbnail))
                        .isInstanceOf(InvalidInputException.class)
                        .hasMessageContaining("Validation failed [while checking null]: Theme.name"),

                () -> assertThatThrownBy(() -> Theme.withoutId(name, null, thumbnail))
                        .isInstanceOf(InvalidInputException.class)
                        .hasMessageContaining("Validation failed [while checking null]: Theme.description"),

                () -> assertThatThrownBy(() -> Theme.withoutId(name, description, null))
                        .isInstanceOf(InvalidInputException.class)
                        .hasMessageContaining("Validation failed [while checking null]: Theme.thumbnail")
        );
    }
} 