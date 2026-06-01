package roomescape.theme.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.common.exception.InactiveException;
import roomescape.common.exception.ValidationException;
import roomescape.theme.domain.Theme;

class ThemeTest {

    @Test
    void 정상적인_테마를_생성한다() {
        // when
        Theme theme = Theme.create("공포테마", "https://image.com/image.png", "무서운 테마입니다.");

        // then
        assertThat(theme).extracting(Theme::getName, Theme::getThumbnailImageUrl, Theme::getDescription,
                        Theme::isActive)
                .containsExactly("공포테마", "https://image.com/image.png", "무서운 테마입니다.", true);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void 테마_이름이_비어있으면_예외가_발생한다(String name) {
        // when & then
        assertThatThrownBy(() -> Theme.create(name, "https://image.com/image.png", "설명"))
                .isInstanceOf(ValidationException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "image.png", "ftp://image.com/image.png"})
    void 이미지_주소가_올바른_HTTP_URL이_아니면_예외가_발생한다(String thumbnailImageUrl) {
        // when & then
        assertThatThrownBy(() -> Theme.create("공포테마", thumbnailImageUrl, "설명"))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    void 비활성화된_테마를_검증하면_예외가_발생한다() {
        // given
        Theme theme = Theme.create("공포테마", "https://image.com/image.png", "설명").deactivate();

        // when & then
        assertThatThrownBy(theme::validateInactive).isInstanceOf(InactiveException.class);
    }
}
