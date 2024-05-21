package roomescape.domain.theme;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class ThemeTest {

    @Test
    @DisplayName("테마를 생성한다.")
    void create() {
        assertThatCode(() -> new Theme("name", "description", "thumbnail"))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @DisplayName("설명이 공백이면 예외가 발생한다.")
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void validateDescription(String description) {
        assertThatThrownBy(() -> new Theme("name", description, "thumbnail"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("설명은 필수 값입니다.");
    }

    @Test
    @DisplayName("설명이 255자를 넘으면 예외가 발생한다.")
    void validateDescriptionLength() {
        String description = "a".repeat(256);

        assertThatThrownBy(() -> new Theme("name", description, "thumbnail"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("설명은 255자를 넘을 수 없습니다.");
    }

    @ParameterizedTest
    @DisplayName("이미지가 공백이면 예외가 발생한다.")
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void validateThumbnail(String thumbnail) {
        assertThatThrownBy(() -> new Theme("name", "description", thumbnail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미지는 필수 값입니다.");
    }

    @Test
    @DisplayName("이미지가 255자를 넘으면 예외가 발생한다.")
    void validateThumbnailLength() {
        String thumbnail = "a".repeat(256);

        assertThatThrownBy(() -> new Theme("name", "description", thumbnail))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미지는 255자를 넘을 수 없습니다.");
    }
}
