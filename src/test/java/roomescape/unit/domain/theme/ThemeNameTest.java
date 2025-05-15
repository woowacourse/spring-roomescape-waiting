package roomescape.unit.domain.theme;

import org.junit.jupiter.api.Test;
import roomescape.domain.theme.ThemeName;

import static org.assertj.core.api.Assertions.*;

class ThemeNameTest {

    @Test
    void null이면_예외를_던진다() {
        assertThatThrownBy(() -> new ThemeName(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void 공백이면_예외를_던진다() {
        assertThatThrownBy(() -> new ThemeName(" "))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 여섯_글자_이상이면_예외를_던진다() {
        assertThatThrownBy(() -> new ThemeName("여섯글자이상"))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 정상_입력이면_생성된다() {
        ThemeName name = new ThemeName("공포");
        assertThat(name.name()).isEqualTo("공포");
    }
}
