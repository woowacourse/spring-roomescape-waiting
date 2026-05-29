package roomescape.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ThemeTest {

    @Test
    @DisplayName("새로운 방탈출 테마를 성공적으로 생성한다.")
    void createThemeTest() {
        // given
        String name = "홍대 방탈출";
        String thumbnailUrl = "https://picsum.photos/400/300";
        String description = "재밌는 방탈출 테마";

        // when
        Theme theme = Theme.create(name, thumbnailUrl, description);

        // then
        assertThat(theme.getId()).isNull();
        assertThat(theme.getName()).isEqualTo("홍대 방탈출");
        assertThat(theme.getThumbnailUrl()).isEqualTo("https://picsum.photos/400/300");
        assertThat(theme.getDescription()).isEqualTo("재밌는 방탈출 테마");
    }

    @Test
    @DisplayName("동일한 ID를 가진 테마는 같은 객체로 판단한다.")
    void equalsAndHashCodeTest() {
        // given
        Theme theme1 = Theme.from(1L, "워너비", "https://picsum.photos/400/001", "설명1");
        Theme theme2 = Theme.from(1L, "어웨이큰", "https://picsum.photos/400/002", "설명2");

        // when & then
        assertThat(theme1).isEqualTo(theme2);
        assertThat(theme1.hashCode()).isEqualTo(theme2.hashCode());
    }
}
