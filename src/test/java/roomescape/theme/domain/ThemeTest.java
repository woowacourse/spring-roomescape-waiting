package roomescape.theme.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ThemeTest {

    @Test
    @DisplayName("정상 테마 생성")
    void 정상_테마_생성() {
        assertThatCode(() -> Theme.of("테마1", "설명", "https://image.com"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("이름이 null이면 예외 발생")
    void 이름_null_예외() {
        assertThatThrownBy(() -> Theme.of(null, "설명", "https://image.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("테마 이름은 필수입니다.");
    }

    @Test
    @DisplayName("이름이 공백이면 예외 발생")
    void 이름_공백_예외() {
        assertThatThrownBy(() -> Theme.of("  ", "설명", "https://image.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("테마 이름은 필수입니다.");
    }

    @Test
    @DisplayName("설명이 null이면 예외 발생")
    void 설명_null_예외() {
        assertThatThrownBy(() -> Theme.of("테마1", null, "https://image.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("테마 설명은 필수입니다.");
    }

    @Test
    @DisplayName("설명이 공백이면 예외 발생")
    void 설명_공백_예외() {
        assertThatThrownBy(() -> Theme.of("테마1", "  ", "https://image.com"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("테마 설명은 필수입니다.");
    }

    @Test
    @DisplayName("이미지 URL이 null이면 예외 발생")
    void 이미지URL_null_예외() {
        assertThatThrownBy(() -> Theme.of("테마1", "설명", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("테마 이미지 URL은 필수입니다.");
    }

    @Test
    @DisplayName("이미지 URL이 공백이면 예외 발생")
    void 이미지URL_공백_예외() {
        assertThatThrownBy(() -> Theme.of("테마1", "설명", "  "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("테마 이미지 URL은 필수입니다.");
    }
}
