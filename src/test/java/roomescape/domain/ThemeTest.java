package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

class ThemeTest {

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"", " "})
    void 빈_이름으로_테마_생성시_예외(String name) {
        // when
        assertThatThrownBy(() -> new Theme(null, name, "설명", "썸네일 경로"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("name은 비어 있을 수 없습니다.");
    }

    @Test
    void 이름이_255자를_초과하면_예외() {
        // given
        String name = "a".repeat(256);

        // when & then
        assertThatThrownBy(() -> new Theme(null, name, "설명", "썸네일 경로"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("name은 255자를 넘을 수 없습니다.");
    }

    @Test
    void 설명이_255자를_초과하면_예외() {
        // given
        String description = "a".repeat(256);

        // when & then
        assertThatThrownBy(() -> new Theme(null, "테마이름", description, "썸네일 경로"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("description은 255자를 넘을 수 없습니다.");
    }

    @Test
    void 테마_생성_성공_테스트() {
        // given
        String name = "테마이름";
        String description = "테마설명";
        String thumbnail = "썸네일 경로";

        // when
        Theme result = new Theme(null, name, description, thumbnail);

        // then
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getDescription()).isEqualTo(description);
        assertThat(result.getThumbnail()).isEqualTo(thumbnail);
    }
}
