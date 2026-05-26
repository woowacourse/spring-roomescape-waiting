package roomescape.domain.reservation.theme;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ThemeTest {
    private final Long id = 1L;
    private final ThemeName name = ThemeName.parse("공포");
    private final Description description = Description.parse("너무무서워");
    private final ThumbnailUrl url = ThumbnailUrl.parse("/images/horror");

    @Test
    @DisplayName("올바른 정보로 테마를 생성하면 성공한다.")
    void 정상_생성_테스트() {
        assertDoesNotThrow(() -> new Theme(id, name, description, url));
    }

    @Test
    @DisplayName("테마 이름이 null이면 예외가 발생한다")
    void 이름_null_예외_테스트() {
        ThemeName name = null;

        assertThatThrownBy(() -> new Theme(id, name, description, url))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("테마 이름이 비어 있습니다.");
    }

    @Test
    @DisplayName("테마 설명이 null이면 예외가 발생한다")
    void 설명_null_예외_테스트() {
        Description description = null;

        assertThatThrownBy(() -> new Theme(id, name, description, url))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("테마 설명이 비어 있습니다.");
    }

    @Test
    @DisplayName("테마 썸네일 주소가 null이면 예외가 발생한다")
    void 썸네일_주소_null_예외_테스트() {
        ThumbnailUrl url = null;

        assertThatThrownBy(() -> new Theme(id, name, description, url))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("테마 썸네일 주소가 비어 있습니다.");
    }
}
