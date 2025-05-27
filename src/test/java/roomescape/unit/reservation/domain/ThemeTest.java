package roomescape.unit.reservation.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import roomescape.exception.ArgumentNullException;
import roomescape.reservation.domain.Theme;

class ThemeTest {

    @Test
    void 이름이_빈_값이면_예외가_발생한다() {
        // when & then
        Assertions.assertThatThrownBy(() -> Theme.builder()
                        .id(1L)
                        .description("des")
                        .thumbnail("thumb").build())
                .isInstanceOf(ArgumentNullException.class);
    }

    @Test
    void 설명이_빈_값이면_예외가_발생한다() {
        // when & then
        Assertions.assertThatThrownBy(() -> Theme.builder()
                        .id(1L)
                        .name("name")
                        .thumbnail("thumb").build())
                .isInstanceOf(ArgumentNullException.class);
    }

    @Test
    void 썸네일이_빈_값이면_예외가_발생한다() {
        // when & then
        Assertions.assertThatThrownBy(() -> Theme.builder()
                        .id(1L)
                        .name("name")
                        .description("des").build())
                .isInstanceOf(ArgumentNullException.class);
    }
}