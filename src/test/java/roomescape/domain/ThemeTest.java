package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.vo.Name;

class ThemeTest {

    @Test
    @DisplayName("모든 필드가 같으면 동등하다")
    void equalWhenAllFieldsSame() {
        Theme theme = new Theme(1L, new Name("테마"), "http://thumbnail", "설명");
        Theme same = new Theme(1L, new Name("테마"), "http://thumbnail", "설명");

        assertThat(theme).isEqualTo(same);
    }

    @Test
    @DisplayName("이름이 다르면 동등하지 않다")
    void notEqualWhenNameDiffers() {
        Theme theme = new Theme(1L, new Name("테마"), "http://thumbnail", "설명");
        Theme different = new Theme(1L, new Name("다른테마"), "http://thumbnail", "설명");

        assertThat(theme).isNotEqualTo(different);
    }
}
