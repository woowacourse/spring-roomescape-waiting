package roomescape.reservation.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThemeThumbnailTest {

    @DisplayName("유효하지 않은 길이의 썸네일이 입력되면 예외를 발생시킨다.")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            " ",
            """
                    아주 길고 이상한 썸네일 이미지 링크...............................................................................
                    아주 길고 이상한 썸네일 이미지 링크...............................................................................
                    아주 길고 이상한 썸네일 이미지 링크...............................................................................
                    아주 길고 이상한 썸네일 이미지 링크...............................................................................
                    아주 길고 이상한 썸네일 이미지 링크...............................................................................
                    아주 길고 이상한 썸네일 이미지 링크...............................................................................
                    아주 길고 이상한 썸네일 이미지 링크.................................................................................
                    """
    })
    void thumbnailLengthTest(final String invalidName) {
        // When & Then
        assertThatThrownBy(() -> new ThemeThumbnail(invalidName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("썸네일은 1글자 이상 700글자 이하여야 합니다.");
    }
}
