package roomescape.theme.domain;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("테마")
class ThemeTest {

    @DisplayName("테마 생성 시 썸네일이 링크 형식이 아닌 경우 예외가 발생한다.")
    @Test
    void validateThumbnailFormat() {
        // given
        String thumbnail = "notLink";

        // when & then
        assertThatThrownBy(() -> new Theme("name", "description", thumbnail));
    }
}
