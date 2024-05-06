package roomescape.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import static org.assertj.core.api.Assertions.assertThat;

class ThemeTest {

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("썸네일이 공백일 경우 기본 썸네일로 대체된다.")
    void getDefaultThumbnailIfNotExists(String thumbnail) {
        // given & when
        final Theme theme = new Theme(null, "spring", "", thumbnail);

        // then
        assertThat(theme.getThumbnail()).isEqualTo("https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");
    }
}
