package roomescape.feature.theme.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.global.error.exception.GeneralException;

class ThemeNameTest {

    @Nested
    class 성공 {

        @Test
        void 유효한_이름으로_생성된다() {
            ThemeName name = new ThemeName("테마 이름");

            assertThat(name.value()).isEqualTo("테마 이름");
        }

        @Test
        void 길이가_255인_이름으로_생성된다() {
            ThemeName name = new ThemeName("a".repeat(255));

            assertThat(name.value()).hasSize(255);
        }
    }

    @Nested
    class 실패 {

        @Test
        void 이름이_null이면_예외가_발생한다() {
            assertThatThrownBy(() -> new ThemeName(null))
                .isInstanceOf(GeneralException.class)
                .hasMessage("테마 이름은 필수이며 255자 이하여야 합니다.");
        }

        @Test
        void 이름이_공백이면_예외가_발생한다() {
            assertThatThrownBy(() -> new ThemeName("   "))
                .isInstanceOf(GeneralException.class)
                .hasMessage("테마 이름은 필수이며 255자 이하여야 합니다.");
        }

        @Test
        void 이름이_빈_문자열이면_예외가_발생한다() {
            assertThatThrownBy(() -> new ThemeName(""))
                .isInstanceOf(GeneralException.class)
                .hasMessage("테마 이름은 필수이며 255자 이하여야 합니다.");
        }

        @Test
        void 이름_길이가_255_초과이면_예외가_발생한다() {
            assertThatThrownBy(() -> new ThemeName("a".repeat(256)))
                .isInstanceOf(GeneralException.class)
                .hasMessage("테마 이름은 필수이며 255자 이하여야 합니다.");
        }
    }
}
