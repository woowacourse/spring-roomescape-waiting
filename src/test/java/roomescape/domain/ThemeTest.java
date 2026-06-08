package roomescape.domain;

import org.junit.jupiter.api.Test;
import roomescape.domain.exception.DomainRuleViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThemeTest {

    @Test
    void 유효한_테마_정보로_테마를_생성하면_필드가_저장된다() {
        Theme theme = new Theme(1L, "공포방", "무서운방입니다.", "image-url");

        assertThat(theme.getName()).isEqualTo("공포방");
        assertThat(theme.getDescription()).isEqualTo("무서운방입니다.");
        assertThat(theme.getThumbnail()).isEqualTo("image-url");
    }

    @Test
    void 이름이_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> new Theme(1L, null, "무서운방입니다.", "image-url"))
                .isInstanceOf(DomainRuleViolationException.class)
                .hasMessage("테마 이름은 비어 있을 수 없습니다.");
    }

    @Test
    void 이름이_공백이면_예외가_발생한다() {
        assertThatThrownBy(() -> new Theme(1L, " ", "무서운방입니다.", "image-url"))
                .isInstanceOf(DomainRuleViolationException.class)
                .hasMessage("테마 이름은 비어 있을 수 없습니다.");
    }
}
