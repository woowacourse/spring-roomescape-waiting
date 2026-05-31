package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ThemeTest {

    @Test
    @DisplayName("정상적인 값을 입력하면 방탈출 테마 객체가 생성된다.")
    void createValidTheme() {
        Theme theme = new Theme(1L, "공포", "귀신의 집 탈출", "https://test.com");
        assertThat(theme.getId()).isEqualTo(1L);
        assertThat(theme.getName()).isEqualTo("공포");
        assertThat(theme.getDescription()).isEqualTo("귀신의 집 탈출");
        assertThat(theme.getThumbnailUrl()).isEqualTo("https://test.com");
    }

    @Test
    @DisplayName("테마 이름이 null이면 예외가 발생한다.")
    void createThemeWithNullNameThrowsException() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Theme(1L, null, "설명", "https://url"))
                .withMessage("테마 이름은 필수입니다.");
    }

    @Test
    @DisplayName("테마 설명이 null이면 예외가 발생한다.")
    void createThemeWithNullDescriptionThrowsException() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Theme(1L, "이름", null, "https://url"))
                .withMessage("테마 설명은 필수입니다.");
    }

    @Test
    @DisplayName("썸네일 URL이 null이면 예외가 발생한다.")
    void createThemeWithNullThumbnailUrlThrowsException() {
        assertThatIllegalArgumentException()
                .isThrownBy(() -> new Theme(1L, "이름", "설명", null))
                .withMessage("썸네일 URL은 필수입니다.");
    }
}
