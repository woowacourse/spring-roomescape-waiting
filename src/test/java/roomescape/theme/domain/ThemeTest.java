package roomescape.theme.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThemeTest {
    @DisplayName("테마 명은 null이 될 수 없다")
    @Test
    void notNullName() {
        // given
        String name = null;
        String description = "desc";
        String thumbnail = "thumbnail";

        // when & then
        assertThatThrownBy(() -> {
            new Theme(name, description, thumbnail);
        }).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("설명은 null이 될 수 없다")
    @Test
    void notNullDescription() {
        // given
        String name = "name";
        String description = null;
        String thumbnail = "thumbnail";

        // when & then
        assertThatThrownBy(() -> {
            new Theme(name, description, thumbnail);
        }).isInstanceOf(IllegalArgumentException.class);
    }

    @DisplayName("테마 명은 null이 될 수 없다")
    @Test
    void notNullThumbnail() {
        // given
        String name = "name";
        String description = "desc";
        String thumbnail = null;

        // when & then
        assertThatThrownBy(() -> {
            new Theme(name, description, thumbnail);
        }).isInstanceOf(IllegalArgumentException.class);
    }
}
