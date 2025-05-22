package roomescape.unit.domain.theme;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.SoftAssertions.*;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeName;
import roomescape.domain.theme.ThemeDescription;
import roomescape.domain.theme.ThemeThumbnail;

class ThemeTest {

    @Test
    void name이_null이면_예외를_던진다() {
        assertThatThrownBy(() ->
                new Theme(1L, null, new ThemeDescription("정상 설명"), new ThemeThumbnail("thumb.jpg")))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void description이_null이면_예외를_던진다() {
        assertThatThrownBy(() ->
                new Theme(1L, new ThemeName("공포"), null, new ThemeThumbnail("thumb.jpg")))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void thumbnail이_null이면_예외를_던진다() {
        assertThatThrownBy(() ->
                new Theme(1L, new ThemeName("공포"), new ThemeDescription("정상 설명"), null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void 정상적으로_생성된다() {
        Theme theme = new Theme(
                1L,
                new ThemeName("공포"),
                new ThemeDescription("무서운 테마입니다"),
                new ThemeThumbnail("thumb.jpg")
        );

        assertSoftly(softly -> {
            softly.assertThat(theme.getName().name()).isEqualTo("공포");
            softly.assertThat(theme.getDescription().description()).isEqualTo("무서운 테마입니다");
            softly.assertThat(theme.getThumbnail().thumbnail()).isEqualTo("thumb.jpg");
        });
    }
}
