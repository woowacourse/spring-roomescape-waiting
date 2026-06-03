package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;

class ThemeTest {

    @Nested
    class 생성 {

        @Test
        void 성공() {
            String name = "방탈출 테마";
            String description = "테스트용 테마 설명입니다.";
            String thumbnailUrl = "https://img.com";

            Theme theme = Theme.create(name, description, thumbnailUrl);

            assertThat(theme.getId()).isNull();
            assertThat(theme.getName()).isEqualTo(name);
            assertThat(theme.getDescription()).isEqualTo(description);
            assertThat(theme.getThumbnailUrl()).isEqualTo(thumbnailUrl);
        }

        @Test
        void withId로_id_부여() {
            Theme theme = Theme.create("방탈출 테마", "테스트용 테마 설명입니다.", "https://img.com");

            Theme withId = theme.withId(3L);

            assertThat(withId.getId()).isEqualTo(3L);
            assertThat(withId.getName()).isEqualTo(theme.getName());
        }

        @Test
        void withId에_null_전달시_예외발생() {
            Theme theme = Theme.create("방탈출 테마", "테스트용 테마 설명입니다.", "https://img.com");

            assertThatThrownBy(() -> theme.withId(null))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.THEME_ID_NULL);
        }

        @Test
        void description이_null이면_예외발생() {
            assertThatThrownBy(() -> Theme.create("방탈출 테마", null, "https://img.com"))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DESCRIPTION_NULL_OR_BLANK);
        }

        @Test
        void description이_빈_문자열이면_예외발생() {
            assertThatThrownBy(() -> Theme.create("방탈출 테마", "", "https://img.com"))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DESCRIPTION_NULL_OR_BLANK);
        }

        @Test
        void description이_5자_미만이면_예외발생() {
            assertThatThrownBy(() -> Theme.create("방탈출 테마", "짧음", "https://img.com"))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DESCRIPTION_TOO_SHORT);
        }

        @Test
        void thumbnailUrl이_null이면_예외발생() {
            assertThatThrownBy(() -> Theme.create("방탈출 테마", "테스트용 테마 설명입니다.", null))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.THUMBNAIL_URL_NULL_OR_BLANK);
        }

        @Test
        void thumbnailUrl이_빈_문자열이면_예외발생() {
            assertThatThrownBy(() -> Theme.create("방탈출 테마", "테스트용 테마 설명입니다.", ""))
                    .isInstanceOf(BusinessException.class)
                    .hasFieldOrPropertyWithValue("errorCode", ErrorCode.THUMBNAIL_URL_NULL_OR_BLANK);
        }
    }
}
