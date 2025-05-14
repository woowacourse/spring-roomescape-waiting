package roomescape.theme.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import roomescape.theme.exception.InvalidThemeException;

class ThemeTest {

    @Test
    void createTheme_shouldThrowException_whenNameLengthOverTen() {
        Assertions.assertThatThrownBy(() -> Theme.withUnassignedId("abcdefghijk", "description", "thumbnail"))
                .isInstanceOf(InvalidThemeException.class)
                .hasMessageContaining("이름은 10글자 이내여야 합니다.");
    }

    @Test
    void createTheme_shouldThrowException_whenDescriptionLengthOverHundred() {
        String description = "a".repeat(101);
        Assertions.assertThatThrownBy(() -> Theme.withUnassignedId("name", description, "thumbnail"))
                .isInstanceOf(InvalidThemeException.class)
                .hasMessageContaining("설명은 100글자 이내여야 합니다.");
    }

    @Test
    void createTheme_shouldThrowException_whenThumbnailLengthOverHundred() {
        String thumbnail = "a".repeat(101);
        Assertions.assertThatThrownBy(() -> Theme.withUnassignedId("name", "description", thumbnail))
                .isInstanceOf(InvalidThemeException.class)
                .hasMessageContaining("썸네일은 100글자 이내여야 합니다.");
    }
}
