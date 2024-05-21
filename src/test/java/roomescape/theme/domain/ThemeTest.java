package roomescape.theme.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ThemeTest {
    @DisplayName("이름이 비어있을 때 예외를 던진다.")
    @Test
    void validateThemeTest_whenNameIsNull() {
        assertThatThrownBy(() ->
                new Theme(1L, null, "오리들과 호랑이들 사이에서 살아남기", "https://image.jpg"))
                .isInstanceOf(NullPointerException.class);
    }

    @DisplayName("설명이 비어있을 때 예외를 던진다.")
    @Test
    void validateThemeTest_whenDescriptionIsNull() {
        assertThatThrownBy(() ->
                new Theme(1L, "오리와 호랑이", null, "https://image.jpg"))
                .isInstanceOf(NullPointerException.class);
    }


    @DisplayName("썸네일이 비어있을 때 예외를 던진다.")
    @Test
    void validateThemeTest_whenThumbnailIsNull() {
        assertThatThrownBy(() ->
                new Theme(1L, "오리와 호랑이", "오리들과 호랑이들 사이에서 살아남기", null))
                .isInstanceOf(NullPointerException.class);
    }
}
