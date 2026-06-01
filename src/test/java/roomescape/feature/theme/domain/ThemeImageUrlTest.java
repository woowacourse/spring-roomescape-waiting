package roomescape.feature.theme.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.global.error.exception.GeneralException;

class ThemeImageUrlTest {

    @Nested
    class 성공 {

        @Test
        void https_URL로_생성된다() {
            ThemeImageUrl imageUrl = new ThemeImageUrl("https://example.com/theme.png");

            assertThat(imageUrl.value()).isEqualTo("https://example.com/theme.png");
        }

        @Test
        void http_URL로_생성된다() {
            ThemeImageUrl imageUrl = new ThemeImageUrl("http://example.com/theme.png");

            assertThat(imageUrl.value()).isEqualTo("http://example.com/theme.png");
        }

        @Test
        void 길이가_2000인_URL로_생성된다() {
            String url = "https://example.com/" + "a".repeat(1980);
            ThemeImageUrl imageUrl = new ThemeImageUrl(url);

            assertThat(imageUrl.value()).hasSize(2000);
        }
    }

    @Nested
    class 실패 {

        @Test
        void URL이_null이면_예외가_발생한다() {
            assertThatThrownBy(() -> new ThemeImageUrl(null))
                .isInstanceOf(GeneralException.class)
                .hasMessage("테마 이미지 URL은 필수이며 2000자 이하의 올바른 URL 형식이어야 합니다.");
        }

        @Test
        void URL이_공백이면_예외가_발생한다() {
            assertThatThrownBy(() -> new ThemeImageUrl("   "))
                .isInstanceOf(GeneralException.class)
                .hasMessage("테마 이미지 URL은 필수이며 2000자 이하의 올바른 URL 형식이어야 합니다.");
        }

        @Test
        void URL이_빈_문자열이면_예외가_발생한다() {
            assertThatThrownBy(() -> new ThemeImageUrl(""))
                .isInstanceOf(GeneralException.class)
                .hasMessage("테마 이미지 URL은 필수이며 2000자 이하의 올바른 URL 형식이어야 합니다.");
        }

        @Test
        void URL_형식이_올바르지_않으면_예외가_발생한다() {
            assertThatThrownBy(() -> new ThemeImageUrl("올바르지않은URL"))
                .isInstanceOf(GeneralException.class)
                .hasMessage("테마 이미지 URL은 필수이며 2000자 이하의 올바른 URL 형식이어야 합니다.");
        }

        @Test
        void http_https_외의_스킴은_예외가_발생한다() {
            assertThatThrownBy(() -> new ThemeImageUrl("ftp://example.com/theme.png"))
                .isInstanceOf(GeneralException.class)
                .hasMessage("테마 이미지 URL은 필수이며 2000자 이하의 올바른 URL 형식이어야 합니다.");
        }

        @Test
        void URL_길이가_2000_초과이면_예외가_발생한다() {
            String url = "https://example.com/" + "a".repeat(1981);
            assertThatThrownBy(() -> new ThemeImageUrl(url))
                .isInstanceOf(GeneralException.class)
                .hasMessage("테마 이미지 URL은 필수이며 2000자 이하의 올바른 URL 형식이어야 합니다.");
        }
    }
}
