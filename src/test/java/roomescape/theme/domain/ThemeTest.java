package roomescape.theme.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class ThemeTest {

    @Test
    void 테마명이_null이면_예외를_발생시킨다() {
        // given
        final String name = null;
        final String description = "테마 설명";
        final String thumbnail = "테마 썸네일";

        // when & then
        Assertions.assertThatThrownBy(() -> new Theme(name, description, thumbnail))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 테마명이_빈_문자열이면_예외를_발생시킨다() {
        // given
        final String name = "";
        final String description = "테마 설명";
        final String thumbnail = "테마 썸네일";

        // when & then
        Assertions.assertThatThrownBy(() -> new Theme(name, description, thumbnail))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 테마_설명이_null이면_예외를_발생시킨다() {
        // given
        final String name = "테마 이름";
        final String description = null;
        final String thumbnail = "테마 썸네일";

        // when & then
        Assertions.assertThatThrownBy(() -> new Theme(name, description, thumbnail))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 테마_설명이_빈_문자열이면_예외를_발생시킨다() {
        // given
        final String name = "테마 이름";
        final String description = "";
        final String thumbnail = "테마 썸네일";

        // when & then
        Assertions.assertThatThrownBy(() -> new Theme(name, description, thumbnail))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 테마_썸네일이_null이면_예외를_발생시킨다() {
        // given
        final String name = "테마 이름";
        final String description = "테마 설명";
        final String thumbnail = null;

        // when & then
        Assertions.assertThatThrownBy(() -> new Theme(name, description, thumbnail))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 테마_썸네일이_빈_문자열이면_예외를_발생시킨다() {
        // given
        final String name = "테마 이름";
        final String description = "테마 설명";
        final String thumbnail = "";

        // when & then
        Assertions.assertThatThrownBy(() -> new Theme(name, description, thumbnail))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
