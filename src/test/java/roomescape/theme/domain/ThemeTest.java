package roomescape.theme.domain;

import org.junit.jupiter.api.Test;
import roomescape.common.exception.DomainException;
import roomescape.common.exception.ErrorPolicy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.theme.exception.ThemeErrorCode.*;

class ThemeTest {

    @Test
    void 테마_이름이_null이면_도메인_예외가_발생한다() {
        assertDomainException(
                () -> Theme.create(null, "설명", "https://example.com/theme.png"),
                INVALID_THEME_NAME
        );
    }

    @Test
    void 테마_이름이_비어있으면_도메인_예외가_발생한다() {
        assertDomainException(
                () -> Theme.create(" ", "설명", "https://example.com/theme.png"),
                INVALID_THEME_NAME
        );
    }

    @Test
    void 테마_설명이_null이면_도메인_예외가_발생한다() {
        assertDomainException(
                () -> Theme.create("테마", null, "https://example.com/theme.png"),
                INVALID_THEME_DESCRIPTION
        );
    }

    @Test
    void 테마_설명이_비어있으면_도메인_예외가_발생한다() {
        assertDomainException(
                () -> Theme.create("테마", " ", "https://example.com/theme.png"),
                INVALID_THEME_DESCRIPTION
        );
    }

    @Test
    void 테마_썸네일이_null이면_도메인_예외가_발생한다() {
        assertDomainException(
                () -> Theme.create("테마", "설명", null),
                INVALID_THEME_THUMBNAIL
        );
    }

    @Test
    void 테마_썸네일이_비어있으면_도메인_예외가_발생한다() {
        assertDomainException(
                () -> Theme.create("테마", "설명", " "),
                INVALID_THEME_THUMBNAIL
        );
    }

    @Test
    void 이미_id가_있는_테마에_id를_부여하면_도메인_예외가_발생한다() {
        Theme theme = Theme.of(1L, "테마", "설명", "https://example.com/theme.png");

        assertDomainException(
                () -> theme.withId(2L),
                THEME_ALREADY_HAS_ID
        );
    }

    private void assertDomainException(Runnable runnable, ErrorPolicy errorCode) {
        assertThatThrownBy(runnable::run)
                .isInstanceOfSatisfying(DomainException.class, exception ->
                        assertThat(exception.getErrorPolicy()).isEqualTo(errorCode)
                )
                .hasMessage(errorCode.message());
    }
}
