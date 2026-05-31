package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import roomescape.exception.CustomException;
import roomescape.exception.ErrorCode;

class ThemeTest {

    @Test
    void 테마_생성() {
        Theme theme = new Theme(1L, "공포의 저택", "버려진 저택에서 탈출하라! 어둠 속에 숨겨진 비밀을 밝혀야 살 수 있다.",
                "https://picsum.photos/seed/haunted/400/250");
        assertThat(theme.getId()).isEqualTo(1L);
        assertThat(theme.getName()).isEqualTo("공포의 저택");
        assertThat(theme.getDescription()).isEqualTo("버려진 저택에서 탈출하라! 어둠 속에 숨겨진 비밀을 밝혀야 살 수 있다.");
        assertThat(theme.getThumbnailUrl()).isEqualTo("https://picsum.photos/seed/haunted/400/250");
    }

    @Test
    void 이름으로_테마를_구별한다() {
        Theme theme = new Theme(1L, "공포의 저택", "설명1", "https://example.com/1.png");
        Theme sameNameTheme = new Theme(2L, "공포의 저택", "설명2", "https://example.com/2.png");
        Theme differentNameTheme = new Theme(3L, "우주 정거장", "설명1", "https://example.com/1.png");

        assertThat(theme).isEqualTo(sameNameTheme);
        assertThat(theme).isNotEqualTo(differentNameTheme);
    }

    @Test
    void 이름이_null이면_예외() {
        assertThatThrownBy(() -> new Theme(1L, null, "설명", "https://example.com/theme.png"))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.THEME_NAME_BLANK.getMessage());
    }

    @Test
    void 이름이_공백이면_예외() {
        assertThatThrownBy(() -> new Theme(1L, "   ", "설명", "https://example.com/theme.png"))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.THEME_NAME_BLANK.getMessage());
    }

    @Test
    void 설명이_null이면_예외() {
        assertThatThrownBy(() -> new Theme(1L, "공포의 저택", null, "https://example.com/theme.png"))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.THEME_DESCRIPTION_BLANK.getMessage());
    }

    @Test
    void 설명이_공백이면_예외() {
        assertThatThrownBy(() -> new Theme(1L, "공포의 저택", "   ", "https://example.com/theme.png"))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.THEME_DESCRIPTION_BLANK.getMessage());
    }

    @Test
    void 썸네일_url이_null이면_예외() {
        assertThatThrownBy(() -> new Theme(1L, "공포의 저택", "설명", null))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.THEME_THUMBNAIL_URL_BLANK.getMessage());
    }

    @Test
    void 썸네일_url이_공백이면_예외() {
        assertThatThrownBy(() -> new Theme(1L, "공포의 저택", "설명", "   "))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.THEME_THUMBNAIL_URL_BLANK.getMessage());
    }
}
