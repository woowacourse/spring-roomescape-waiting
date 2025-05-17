package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ThemeTest {

    @Nested
    class SuccessTest {

        @Test
        @DisplayName("동일한 id를 가진 테마는 동등하다")
        void themes_with_same_id_are_equal() {
            Theme theme1 = new Theme(1L, "A", "desc", "thumb.jpg");
            Theme theme2 = new Theme(1L, "B", "desc2", "thumb2.jpg");

            assertThat(theme1).isEqualTo(theme2);
        }

        @Test
        @DisplayName("다른 id를 가진 테마는 동등하지 않다")
        void themes_with_different_id_are_not_equal() {
            Theme theme1 = new Theme(1L, "A", "desc", "thumb.jpg");
            Theme theme2 = new Theme(2L, "A", "desc", "thumb.jpg");

            assertThat(theme1).isNotEqualTo(theme2);
        }

        @Test
        @DisplayName("id가 null인 테마는 동등하지 않다")
        void themes_with_null_id_are_not_equal() {
            Theme theme1 = new Theme(null, "A", "desc", "thumb.jpg");
            Theme theme2 = new Theme(null, "A", "desc", "thumb.jpg");

            assertThat(theme1).isNotEqualTo(theme2);
        }
    }
}
