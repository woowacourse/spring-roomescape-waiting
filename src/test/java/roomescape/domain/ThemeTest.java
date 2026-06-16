package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.domain.fixture.ThemeFixture;
import roomescape.exception.RoomEscapeException;

class ThemeTest {

    @Test
    void 정상적인_테마_정보를_생성한다() {
        // given
        String name = "공포의 방";
        String description = "정말 무시무시한 공포 테마입니다.";
        String thumbnailImageUrl = "https://image.com/horror.png";
        Long price = 30000L;

        // when
        Theme theme = Theme.create(name, description, thumbnailImageUrl, price);

        // then: 기본 생성 시 삭제되지 않은 상태이다.
        assertThat(theme)
                .extracting(Theme::getName, Theme::getDescription, Theme::getThumbnailImageUrl, Theme::getPrice,
                        Theme::isActive)
                .containsExactly(name, description, thumbnailImageUrl, price, true);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    void 테마_이름이_비어있을_경우_예외가_발생한다(String invalidName) {
        // given
        String description = "설명";
        String thumbnailImageUrl = "https://image.com/test.png";

        // when & then
        assertThatThrownBy(() -> Theme.create(invalidName, description, thumbnailImageUrl, 30000L))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("이름은 필수 값입니다.");
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1, -10000})
    void 테마_금액이_0이하이면_예외가_발생한다(long invalidPrice) {
        // given
        String name = "테마 이름";
        String description = "설명";
        String thumbnailImageUrl = "https://image.com/test.png";

        // when & then
        assertThatThrownBy(() -> Theme.create(name, description, thumbnailImageUrl, invalidPrice))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("금액은 양수여야 합니다.");
    }

    @Test
    void 테마_금액이_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> Theme.create("테마 이름", "설명", "https://image.com/test.png", null))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("금액은 필수 값입니다.");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "  "})
    void 테마_설명이_비어있을_경우_예외가_발생한다(String invalidDescription) {
        // given
        String name = "테마 이름";
        String thumbnailImageUrl = "https://image.com/test.png";

        // when & then
        assertThatThrownBy(() -> Theme.create(name, invalidDescription, thumbnailImageUrl, 30000L))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage("설명은 필수 값입니다.");
    }

    @ParameterizedTest(name = "이미지 주소가 ''{0}''일 때, \"{1}\" 메시지와 함께 예외가 발생한다.")
    @CsvSource(value = {
            "null, 올바른 이미지 주소 형식이 아닙니다.",
            "'', 올바른 이미지 주소 형식이 아닙니다.",
            "' ', 올바른 이미지 주소 형식이 아닙니다.",
            "문자열, 올바른 이미지 주소 형식이 아닙니다.",                       // 순수 문자열
            "ftp://image.com/test.png, 올바른 이미지 주소 형식이 아닙니다.",   // 다른 프로토콜 스킴
            "htts://image.com/test.png, 올바른 이미지 주소 형식이 아닙니다."   // 오탈자
    }, nullValues = "null")
    void 썸네일_이미지_주소_검증_통합_테스트(String invalidUrl, String expectedMessage) {
        // given
        String name = "테마 이름";
        String description = "설명";

        // when & then
        assertThatThrownBy(() -> Theme.create(name, description, invalidUrl, 30000L))
                .isInstanceOf(RoomEscapeException.class)
                .hasMessage(expectedMessage);
    }

    @Test
    void 테마를_비활성화_할_수_있다() {
        // given
        Theme theme = ThemeFixture.createDefaultTheme();

        // when
        theme.deactivate();

        // then
        assertThat(theme.isActive()).isFalse();
    }

    @Test
    void 이미_비활성화된_테마를_다시_비활성화하면_예외가_발생한다() {
        // given
        Theme theme = ThemeFixture.createDefaultTheme();
        theme.deactivate();

        // when & then
        assertThatThrownBy(theme::deactivate)
                .isInstanceOf(RoomEscapeException.class)
                .hasMessageContaining("이미 비활성화 된 테마입니다.");
    }

    @Test
    void 테마를_활성화_할_수_있다() {
        // given
        Theme theme = ThemeFixture.createDefaultTheme();
        theme.deactivate();

        // when
        theme.activate();

        // then
        assertThat(theme.isActive()).isTrue();
    }

    @Test
    void 이미_활성화된_테마를_다시_활성화하면_예외가_발생한다() {
        // given
        Theme theme = ThemeFixture.createDefaultTheme();

        // when & then
        assertThatThrownBy(theme::activate)
                .isInstanceOf(RoomEscapeException.class)
                .hasMessageContaining("이미 활성화 된 테마입니다.");
    }
}
