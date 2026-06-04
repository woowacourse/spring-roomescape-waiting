package roomescape.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import roomescape.exception.BusinessException;
import roomescape.exception.ErrorCode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThemeTest {

    @Test
    void Theme_객체_생성() {
        final Theme theme = Theme.create("우테코", "우테코는 재밌어", "https://wooteco.com/thumbnail.jpg");

        assertThat(theme.getName()).isEqualTo("우테코");
        assertThat(theme.getDescription()).isEqualTo("우테코는 재밌어");
        assertThat(theme.getThumbnailUrl()).isEqualTo("https://wooteco.com/thumbnail.jpg");
    }

    @ParameterizedTest
    @NullAndEmptySource
    void 설명이_null이거나_비어있으면_예외발생(final String description) {
        assertThatThrownBy(() -> Theme.create("우테코", description, "https://wooteco.com/thumbnail.jpg"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DESCRIPTION_NULL_OR_BLANK);
    }

    @Test
    void 설명이_너무_짧으면_예외발생() {
        assertThatThrownBy(() -> Theme.create("우테코", "짧다", "https://wooteco.com/thumbnail.jpg"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DESCRIPTION_TOO_SHORT);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void 썸네일_URL이_null이거나_비어있으면_예외발생(final String thumbnailUrl) {
        assertThatThrownBy(() -> Theme.create("우테코", "우테코는 재밌어", thumbnailUrl))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.THUMBNAIL_URL_NULL_OR_BLANK);
    }

    @Test
    void ID를_포함한_Theme_객체_생성() {
        final Theme theme = Theme.createWithId(1L, "우테코", "우테코는 재밌어", "https://wooteco.com/thumbnail.jpg");

        assertThat(theme.getId()).isEqualTo(1L);
    }

    @Test
    void ID가_null이면_예외발생() {
        assertThatThrownBy(() -> Theme.createWithId(null, "우테코", "우테코는 재밌어", "https://wooteco.com/thumbnail.jpg"))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.THEME_ID_NULL);
    }

    @Test
    void ID를_변경한_새로운_Theme_객체_반환() {
        final Theme theme = Theme.create("우테코", "우테코는 재밌어", "https://wooteco.com/thumbnail.jpg");

        final Theme themeWithId = theme.withId(1L);

        assertThat(themeWithId.getId()).isEqualTo(1L);
        assertThat(themeWithId.getName()).isEqualTo(theme.getName());
    }
}
