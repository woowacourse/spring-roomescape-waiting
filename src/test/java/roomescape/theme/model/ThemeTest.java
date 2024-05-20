package roomescape.theme.model;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

class ThemeTest {

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("테마 생성 시 테마 명이 빈 값인 경우 예외가 발생한다.")
    void createTheme_WhenThemeNameIsBlank(String name) {
        assertThatThrownBy(() -> new Theme(1L, name, "aa", "https://asd.dsd"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("테마 생성 시 테마 명은 필수입니다.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("테마 생성 시 테마 명이 빈 값인 경우 예외가 발생한다.")
    void createTheme_WhenThemeDescriptionIsBlank(String description) {
        assertThatThrownBy(() -> new Theme(1L, "aa", description, "https://asd.dsd"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("테마 생성 시 테마 설명은 필수입니다.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("테마 생성 시 테마 명이 빈 값인 경우 예외가 발생한다.")
    void createTheme_WhenThemeThumbnailIsBlank(String thumbnail) {
        assertThatThrownBy(() -> new Theme(1L, "aa", "설명이다", thumbnail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("테마 생성 시 썸네일은 필수입니다.");
    }

}
