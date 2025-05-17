package roomescape.unit.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import roomescape.domain.Theme;
import roomescape.exception.ArgumentNullException;

class ThemeTest {

    @Test
    void 이름이_빈_값이면_예외가_발생한다() {
        // when & then
        Assertions.assertThatThrownBy(() -> new Theme(1L, null, "des", "thumb"))
                .isInstanceOf(ArgumentNullException.class);
    }

    @Test
    void 설명이_빈_값이면_예외가_발생한다() {
        // when & then
        Assertions.assertThatThrownBy(() -> new Theme(1L, "name", null, "thumb"))
                .isInstanceOf(ArgumentNullException.class);
    }

    @Test
    void 썸네일이_빈_값이면_예외가_발생한다() {
        // when & then
        Assertions.assertThatThrownBy(() -> new Theme(1L, "name", "des", null))
                .isInstanceOf(ArgumentNullException.class);
    }
}