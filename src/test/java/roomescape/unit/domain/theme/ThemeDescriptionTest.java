package roomescape.unit.domain.theme;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import roomescape.domain.theme.ThemeDescription;

class ThemeDescriptionTest {

    @Test
    void null이면_예외를_던진다() {
        // when // then
        assertThatThrownBy(() -> new ThemeDescription(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void 삼십일자_이상이면_예외를_던진다() {
        // given
        var longText = "1234567890123456789012345678901";

        // when // then
        assertThatThrownBy(() -> new ThemeDescription(longText))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void 정상_입력이면_생성된다() {
        // given
        var description = new ThemeDescription("짧은 설명");

        // when // then
        assertThat(description.description()).isEqualTo("짧은 설명");
    }
}
