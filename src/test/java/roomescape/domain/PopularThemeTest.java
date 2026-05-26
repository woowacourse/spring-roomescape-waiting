package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import roomescape.exception.InvalidDomainException;

class PopularThemeTest {

    private static final Theme THEME = new Theme(1L, "테마", "설명", "https://thumbnail.url");

    @Test
    void 테마가_null이면_예외() {
        assertThatThrownBy(() -> new PopularTheme(null, 1L))
                .isInstanceOf(InvalidDomainException.class)
                .hasMessage("테마는 필수입니다.");
    }

    @Test
    void 예약_수가_음수면_예외() {
        assertThatThrownBy(() -> new PopularTheme(THEME, -1L))
                .isInstanceOf(InvalidDomainException.class)
                .hasMessage("예약 수는 음수일 수 없습니다.");
    }
}