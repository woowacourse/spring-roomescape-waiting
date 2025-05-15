package roomescape.unit.domain.theme;

import org.junit.jupiter.api.Test;
import roomescape.domain.theme.ThemeDescription;

import static org.assertj.core.api.Assertions.*;

class ThemeDescriptionTest {

    @Test
    void null이면_예외를_던진다() {
        assertThatThrownBy(() -> new ThemeDescription(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void 삼십일자_이상이면_예외를_던진다() {
        String longText = "1234567890123456789012345678901";
        assertThatThrownBy(() -> new ThemeDescription(longText))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 정상_입력이면_생성된다() {
        ThemeDescription description = new ThemeDescription("짧은 설명");
        assertThat(description.description()).isEqualTo("짧은 설명");
    }
}
