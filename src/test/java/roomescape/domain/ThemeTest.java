package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class ThemeTest {

    @Test
    void name이_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> new Theme(1L, null, "설명", "https://thumb.com"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void description이_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> new Theme(1L, "방탈출1", null, "https://thumb.com"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void thumbnail이_null이면_예외가_발생한다() {
        assertThatThrownBy(() -> new Theme(1L, "방탈출1", "설명", null))
                .isInstanceOf(NullPointerException.class);
    }
}
