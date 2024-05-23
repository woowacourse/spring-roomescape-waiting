package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.util.Fixture.HORROR_DESCRIPTION;
import static roomescape.util.Fixture.HORROR_THEME_NAME;
import static roomescape.util.Fixture.THUMBNAIL;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PopularThemesTest {

    @DisplayName("상위 n 인기 테마들을 반환한다.")
    @Test
    void findPopularThemesTopOf() {
        Theme theme1 = new Theme(1L, new ThemeName(HORROR_THEME_NAME), new Description(HORROR_DESCRIPTION), THUMBNAIL);
        Theme theme2 = new Theme(2L, new ThemeName("액션"), new Description("액션 탈출"), THUMBNAIL);
        Theme theme3 = new Theme(3L, new ThemeName("액션"), new Description("액션 탈출"), THUMBNAIL);

        PopularThemes popularThemes = new PopularThemes(List.of(theme1, theme2, theme3));

        List<Theme> popularThemesCountOf = popularThemes.findPopularThemesTopOf(1);

        assertThat(popularThemesCountOf.get(0).getName()).isEqualTo("액션");
    }
}
