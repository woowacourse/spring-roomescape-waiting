package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import roomescape.domain.exception.InvalidInputException;

class ThemeTest {

    @Test
    void 테마_생성() {
        Theme theme = new Theme(1L, "공포의 저택", "설명", "https://example.com/img.jpg");

        assertThat(theme.getId()).isEqualTo(1L);
        assertThat(theme.getName()).isEqualTo("공포의 저택");
        assertThat(theme.getDescription()).isEqualTo("설명");
        assertThat(theme.getThumbnailUrl()).isEqualTo("https://example.com/img.jpg");
    }

    @Test
    void 이름이_null이면_예외() {
        assertThatThrownBy(() -> new Theme(1L, null, "설명", "https://example.com/img.jpg"))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void 이름이_공백이면_예외() {
        assertThatThrownBy(() -> new Theme(1L, "   ", "설명", "https://example.com/img.jpg"))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void 설명이_null이면_예외() {
        assertThatThrownBy(() -> new Theme(1L, "공포의 저택", null, "https://example.com/img.jpg"))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void 설명이_공백이면_예외() {
        assertThatThrownBy(() -> new Theme(1L, "공포의 저택", "   ", "https://example.com/img.jpg"))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void 썸네일_URL이_null이면_예외() {
        assertThatThrownBy(() -> new Theme(1L, "공포의 저택", "설명", null))
                .isInstanceOf(InvalidInputException.class);
    }

    @Test
    void 썸네일_URL이_공백이면_예외() {
        assertThatThrownBy(() -> new Theme(1L, "공포의 저택", "설명", "   "))
                .isInstanceOf(InvalidInputException.class);
    }
}
