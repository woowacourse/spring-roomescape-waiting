package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PopularThemesTest {

    @Test
    @DisplayName("테마 리스트가 들어가면 인기 테마 10개를 조회한다.")
    void makePopularThemes() {
        Theme theme1 = new Theme("a", "a", "a");
        Theme theme2 = new Theme("b", "b", "b");
        List<Theme> themes = new ArrayList<>();
        themes.add(theme1);
        themes.add(theme1);
        themes.add(theme2);
        PopularThemes popularTheme = new PopularThemes(themes);
        List<Theme> popularThemes = popularTheme.getPopularThemes();

        assertAll(
                () -> assertThat(popularThemes.size()).isEqualTo(2),
                () -> assertThat(popularThemes.get(0).getName()).isEqualTo("a")
        );

    }

}
