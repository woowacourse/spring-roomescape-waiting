package roomescape.theme.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ThemeThumbnailTest {
    @DisplayName("테마 설명이 null일 경우 예외를 던진다.")
    @Test
    void validateTest_whenValueIsNull() {
        assertThatThrownBy(() -> new ThemeThumbnail(null))
                .isInstanceOf(NullPointerException.class);
    }

    @DisplayName("테마 설명이 1글자 미만일 경우 예외를 던진다.")
    @Test
    void validateTest_whenValueIsEmpty() {
        assertThatThrownBy(() -> new ThemeThumbnail(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("테마 썸네일은 1글자 이상 500글자 이하이어야 합니다.");
    }

    @DisplayName("테마 설명이 500글자 초과일 경우 예외를 던진다.")
    @Test
    void validateTest_whenValueIsLong() {
        assertThatThrownBy(() -> new ThemeThumbnail("a".repeat(501)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("테마 썸네일은 1글자 이상 500글자 이하이어야 합니다.");
    }
}
