package roomescape.theme.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("테마 썸네일")
class ThemeThumbnailTest {

    @DisplayName("테마 썸네일은 링크 형식이 들어오면 생성된다.")
    @ValueSource(strings = {"http://example.com", "https://example.com"})
    @ParameterizedTest
    void createWithLinkThumbnail(String thumbnail) {
        // when & then
        assertThatCode(() -> new ThemeThumbnail(thumbnail))
                .doesNotThrowAnyException();
    }

    @DisplayName("테마 썸네일은 링크 형식이 아닌 썸네일이 들어올 경우 예외가 발생한다.")
    @Test
    void validateThumbnailFormat() {
        // given
        String thumbnail = "notLink";

        // when & then
        assertThatThrownBy(() -> new ThemeThumbnail(thumbnail));
    }
}
