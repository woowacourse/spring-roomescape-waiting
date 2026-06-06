package roomescape.domain.theme;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("테마")
class ThemeTest {

    @Test
    @DisplayName("생성하면 식별자 없이 테마 정보를 담는다")
    void create() {
        // when
        Theme theme = Theme.create("심해 공포", "심해 탈출 공포 테마", "/themes/deep-sea");

        // then
        assertThat(theme.getId()).isNull();
        assertThat(theme.getName()).isEqualTo("심해 공포");
        assertThat(theme.getDescription()).isEqualTo("심해 탈출 공포 테마");
        assertThat(theme.getThumbnailUrl()).isEqualTo("/themes/deep-sea");
    }

    @Test
    @DisplayName("조회 결과를 그대로 담는다")
    void of() {
        // when
        Theme theme = Theme.of(1L, "도심 추격전", "도심에서 벌어지는 추격 테마", "/themes/chase");

        // then
        assertThat(theme.getId()).isEqualTo(1L);
        assertThat(theme.getName()).isEqualTo("도심 추격전");
        assertThat(theme.getDescription()).isEqualTo("도심에서 벌어지는 추격 테마");
        assertThat(theme.getThumbnailUrl()).isEqualTo("/themes/chase");
    }
}
