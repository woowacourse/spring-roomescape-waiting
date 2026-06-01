package roomescape.theme.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ThemeTest {

    @Test
    @DisplayName("ID가 같으면 동일한 엔티티로 취급한다.")
    void equalsAndHashCode() {
        Theme theme1 = new Theme(1L, "테마1", "설명1", "url1");
        Theme theme2 = new Theme(1L, "테마2", "설명2", "url2");

        assertThat(theme1).isEqualTo(theme2);
        assertThat(theme1.hashCode()).isEqualTo(theme2.hashCode());
    }

    @Test
    @DisplayName("ID가 다르면 다른 엔티티로 취급한다.")
    void equalsAndHashCode_differentId() {
        Theme theme1 = new Theme(1L, "테마", "설명", "url");
        Theme theme2 = new Theme(2L, "테마", "설명", "url");

        assertThat(theme1).isNotEqualTo(theme2);
    }
}
