package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.exception.theme.ThemeFieldRequiredException;

class ThemeTest {

    @DisplayName("테마는 빈 이름으로 생성할 수 없다")
    @Test
    void themeNameTest() {
        // given
        String name = "";
        String description = "별이 빛나는 밤";
        String thumbnail = "/image/star";

        // when & then
        assertThatThrownBy(() -> new Theme(name, description, thumbnail))
                .isInstanceOf(ThemeFieldRequiredException.class);
    }

    @DisplayName("테마는 빈 설명으로 생성할 수 없다")
    @Test
    void themeDescriptionTest() {
        // given
        String name = "에드";
        String description = "";
        String thumbnail = "/image/star";

        // when & then
        assertThatThrownBy(() -> new Theme(name, description, thumbnail))
                .isInstanceOf(ThemeFieldRequiredException.class);
    }

    @DisplayName("테마는 빈 썸네일로 생성할 수 없다")
    @Test
    void themeThumbnailTest() {
        // given
        String name = "에드";
        String description = "별이 빛나는 밤";
        String thumbnail = "";

        // when & then
        assertThatThrownBy(() -> new Theme(name, description, thumbnail))
                .isInstanceOf(ThemeFieldRequiredException.class);
    }
}
