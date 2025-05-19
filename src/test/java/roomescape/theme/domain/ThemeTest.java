package roomescape.theme.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.validate.InvalidInputException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThemeTest {

    @Test
    @DisplayName("테마 이름이 null이면 예외가 발생한다")
    void throwExceptionWhenThemeNameIsNull() {
        // given
        ThemeDescription themeDescription = ThemeDescription.from("설명");
        ThemeThumbnail themeThumbnail = ThemeThumbnail.from("thumbnail.jpg");

        // when & then
        assertThatThrownBy(() -> Theme.withoutId(null, themeDescription, themeThumbnail))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Validation failed [while checking null]: Theme.name");
    }

    @Test
    @DisplayName("테마 설명이 null이면 예외가 발생한다")
    void throwExceptionWhenThemeDescriptionIsNull() {
        // given
        ThemeName themeName = ThemeName.from("테마명");
        ThemeThumbnail themeThumbnail = ThemeThumbnail.from("thumbnail.jpg");

        // when & then
        assertThatThrownBy(() -> Theme.withoutId(themeName, null, themeThumbnail))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Validation failed [while checking null]: Theme.description");

    }

    @Test
    @DisplayName("테마 썸네일이 null이면 예외가 발생한다")
    void throwExceptionWhenThemeThumbnailIsNull() {
        // given
        ThemeName themeName = ThemeName.from("테마명");
        ThemeDescription themeDescription = ThemeDescription.from("설명");

        // when & then
        assertThatThrownBy(() -> Theme.withoutId(themeName, themeDescription, null))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Validation failed [while checking null]: Theme.thumbnail");
    }

    @Test
    @DisplayName("테마 ID가 null이면 예외가 발생한다")
    void throwExceptionWhenThemeIdIsNull() {
        // given
        ThemeName themeName = ThemeName.from("테마명");
        ThemeDescription themeDescription = ThemeDescription.from("설명");
        ThemeThumbnail themeThumbnail = ThemeThumbnail.from("thumbnail.jpg");

        // when & then
        assertThatThrownBy(() -> Theme.withId(null, themeName, themeDescription, themeThumbnail))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Validation failed [while checking null]: Theme.id");
    }
}
