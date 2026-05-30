package roomescape.feature.theme.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.global.error.exception.GeneralException;

class ThemeDescriptionTest {

    @Nested
    class 성공 {

        @Test
        void 유효한_설명으로_생성된다() {
            ThemeDescription description = new ThemeDescription("테마 설명");

            assertThat(description.value()).isEqualTo("테마 설명");
        }

        @Test
        void 길이가_255인_설명으로_생성된다() {
            ThemeDescription description = new ThemeDescription("a".repeat(255));

            assertThat(description.value()).hasSize(255);
        }
    }

    @Nested
    class 실패 {

        @Test
        void 설명이_null이면_예외가_발생한다() {
            assertThatThrownBy(() -> new ThemeDescription(null))
                .isInstanceOf(GeneralException.class)
                .hasMessage("테마 설명은 필수이며 255자 이하여야 합니다.");
        }

        @Test
        void 설명이_공백이면_예외가_발생한다() {
            assertThatThrownBy(() -> new ThemeDescription("   "))
                .isInstanceOf(GeneralException.class)
                .hasMessage("테마 설명은 필수이며 255자 이하여야 합니다.");
        }

        @Test
        void 설명이_빈_문자열이면_예외가_발생한다() {
            assertThatThrownBy(() -> new ThemeDescription(""))
                .isInstanceOf(GeneralException.class)
                .hasMessage("테마 설명은 필수이며 255자 이하여야 합니다.");
        }

        @Test
        void 설명_길이가_255_초과이면_예외가_발생한다() {
            assertThatThrownBy(() -> new ThemeDescription("a".repeat(256)))
                .isInstanceOf(GeneralException.class)
                .hasMessage("테마 설명은 필수이며 255자 이하여야 합니다.");
        }
    }
}
