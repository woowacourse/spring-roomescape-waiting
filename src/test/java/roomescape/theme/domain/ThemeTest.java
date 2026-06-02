package roomescape.theme.domain;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import roomescape.common.exception.RoomEscapeException;
import roomescape.theme.exception.ThemeErrorCode;

class ThemeTest {

    @Test
    void 이름이_비어있으면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> Theme.create("", "귀신을 찾는 테마입니다.", "https://image.png"))
                .isInstanceOf(RoomEscapeException.class)
                .extracting("errorCode")
                .isEqualTo(ThemeErrorCode.THEME_INVALID_NAME);

    }

    @Test
    void 설명이_비어있으면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> Theme.create("귀신 찾기", "", "https://image.png"))
                .isInstanceOf(RoomEscapeException.class)
                .extracting("errorCode")
                .isEqualTo(ThemeErrorCode.THEME_INVALID_DESCRIPTION);
    }

    @Test
    void 설명이_3자_미만이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> Theme.create("귀신 찾기", "귀신", "https://image.png"))
                .isInstanceOf(RoomEscapeException.class)
                .extracting("errorCode")
                .isEqualTo(ThemeErrorCode.THEME_INVALID_DESCRIPTION);
    }

    @Test
    void 이미지_URL이_비어있으면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> Theme.create("귀신 찾기", "귀신을 찾는 게임입니다.", ""))
                .isInstanceOf(RoomEscapeException.class)
                .extracting("errorCode")
                .isEqualTo(ThemeErrorCode.THEME_INVALID_URL);
    }

    @Test
    void 이미지_URL에_점이_없으면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> Theme.create("귀신 찾기", "귀신을 찾는 게임입니다.", "https://image"))
                .isInstanceOf(RoomEscapeException.class)
                .extracting("errorCode")
                .isEqualTo(ThemeErrorCode.THEME_INVALID_URL);
    }
}
