package roomescape.theme.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.InvalidRequestException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThemeTest {

    @Test
    @DisplayName("테마 이름이 비어있으면 도메인 예외가 발생한다.")
    void create_fail_whenNameIsBlank() {
        assertInvalidRequestException(
                () -> new Theme(" ", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png")
        );
    }

    @Test
    @DisplayName("테마 설명이 비어있으면 도메인 예외가 발생한다.")
    void create_fail_whenDescriptionIsBlank() {
        assertInvalidRequestException(
                () -> new Theme("레벨2 탈출", " ", "https://example.com/theme.png")
        );
    }

    @Test
    @DisplayName("테마 썸네일이 비어있으면 도메인 예외가 발생한다.")
    void create_fail_whenThumbnailIsBlank() {
        assertInvalidRequestException(
                () -> new Theme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", " ")
        );
    }

    @Test
    @DisplayName("테마 id가 null이면 도메인 예외가 발생한다.")
    void withId_fail_whenIdIsNull() {
        Theme theme = new Theme("레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");

        assertInvalidRequestException(
                () -> theme.withId(null)
        );
    }

    @Test
    @DisplayName("이미 id가 있는 테마에 id를 부여하면 도메인 예외가 발생한다.")
    void withId_fail_whenThemeAlreadyHasId() {
        Theme theme = new Theme(1L, "레벨2 탈출", "우테코 레벨2를 탈출하는 내용입니다.", "https://example.com/theme.png");

        assertInvalidRequestException(
                () -> theme.withId(2L)
        );
    }

    private void assertInvalidRequestException(Runnable runnable) {
        assertThatThrownBy(runnable::run)
                .isInstanceOf(InvalidRequestException.class);
    }
}
