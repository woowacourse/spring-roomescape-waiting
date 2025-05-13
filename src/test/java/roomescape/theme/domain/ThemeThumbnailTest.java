package roomescape.theme.domain;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.validate.InvalidInputException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThemeThumbnailTest {

    @Test
    @DisplayName("테마 썸네일 URI가 null이면 예외가 발생한다")
    void validateNullThemeThumbnail() {
        // when
        // then
        assertThatThrownBy(() -> ThemeThumbnail.from(null))
                .isInstanceOf(InvalidInputException.class)
                .hasMessage("Validation failed [while checking blank]: ThemeThumbnail.value");
    }

    @Test
    @DisplayName("테마 썸네일 URI가 빈 문자열이면 예외가 발생한다")
    void validateBlankThemeThumbnail() {
        // when
        // then
        Assertions.assertAll(() -> {
            assertThatThrownBy(() -> ThemeThumbnail.from(""))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("Validation failed [while checking blank]: ThemeThumbnail.value");

            assertThatThrownBy(() -> ThemeThumbnail.from(" "))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("Validation failed [while checking blank]: ThemeThumbnail.value");
        });
    }

    @Test
    @DisplayName("테마 썸네일 URI 형식이 올바르지 않으면 예외가 발생한다")
    void validateInvalidUriFormat() {
        // when
        // then
        Assertions.assertAll(() -> {
            assertThatThrownBy(() -> ThemeThumbnail.from("invalid uri format"))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("Validation failed [while checking URI]: ThemeThumbnail.value");

            assertThatThrownBy(() -> ThemeThumbnail.from("http://invalid uri.com"))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("Validation failed [while checking URI]: ThemeThumbnail.value");
        });
    }

    @Test
    @DisplayName("유효한 테마 썸네일 URI로 ThemeThumbnail 객체를 생성할 수 있다")
    void createValidThemeThumbnail() {
        // when
        final ThemeThumbnail themeThumbnail1 = ThemeThumbnail.from("https://example.com/image.jpg");
        final ThemeThumbnail themeThumbnail2 = ThemeThumbnail.from("http://example.com/image.jpg");
        final ThemeThumbnail themeThumbnail3 = ThemeThumbnail.from("file:///path/to/image.jpg");

        // then
        Assertions.assertAll(() -> {
            assertThat(themeThumbnail1).isNotNull();
            assertThat(themeThumbnail1.getValue().toString()).isEqualTo("https://example.com/image.jpg");

            assertThat(themeThumbnail2).isNotNull();
            assertThat(themeThumbnail2.getValue().toString()).isEqualTo("http://example.com/image.jpg");

            assertThat(themeThumbnail3).isNotNull();
            assertThat(themeThumbnail3.getValue().toString()).isEqualTo("file:///path/to/image.jpg");
        });
    }
}
