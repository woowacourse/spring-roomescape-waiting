package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.infrastructure.error.exception.ThemeException;

class ThemeTest {

    @Test
    @DisplayName("정상적인 테마 정보로 Theme를 생성할 수 있다.")
    void 정상_테마_생성() {
        assertThatCode(() -> new Theme("이름", "테마 설명", "썸네일"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("이름이 비어있으면 예외가 발생한다.")
    void 이름이_비어있으면_예외() {
        assertThatThrownBy(() -> new Theme(" ", "설명", "썸네일"))
                .isInstanceOf(ThemeException.class)
                .hasMessage("테마 이름은 비어있을 수 없습니다.");
    }

    @Test
    @DisplayName("이름이 너무 길면 예외가 발생한다.")
    void 이름이_너무_길면_예외() {
        // given
        String longName = "a".repeat(51);

        // when
        // then
        assertThatThrownBy(() -> new Theme(longName, "설명", "썸네일"))
                .isInstanceOf(ThemeException.class)
                .hasMessage("테마 이름은 50자 이하여야 합니다.");
    }

    @Test
    @DisplayName("설명이 비어있으면 예외가 발생한다.")
    void 설명이_비어있으면_예외() {
        assertThatThrownBy(() -> new Theme("이름", " ", "썸네일"))
                .isInstanceOf(ThemeException.class)
                .hasMessage("테마 설명은 비어있을 수 없습니다.");
    }

    @Test
    @DisplayName("설명이 너무 길면 예외가 발생한다.")
    void 설명이_너무_길면_예외() {
        // given
        String longDescription = "a".repeat(201);

        // when
        // then
        assertThatThrownBy(() -> new Theme("이름", longDescription, "썸네일"))
                .isInstanceOf(ThemeException.class)
                .hasMessage("테마 설명은 200자 이하여야 합니다.");
    }

    @Test
    @DisplayName("썸네일이 비어있으면 예외가 발생한다.")
    void 썸네일이_비어있으면_예외() {
        assertThatThrownBy(() -> new Theme("이름", "설명", " "))
                .isInstanceOf(ThemeException.class)
                .hasMessage("테마 썸네일은 비어있을 수 없습니다.");
    }

    @Test
    @DisplayName("썸네일이 너무 길면 예외가 발생한다.")
    void 썸네일이_너무_길면_예외() {
        // given
        String longThumbnail = "a".repeat(201);

        // when
        // then
        assertThatThrownBy(() -> new Theme("이름", "설명", longThumbnail))
                .isInstanceOf(ThemeException.class)
                .hasMessage("테마 썸네일은 200자 이하여야 합니다.");
    }
}
