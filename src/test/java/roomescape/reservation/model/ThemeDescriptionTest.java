package roomescape.reservation.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThemeDescriptionTest {

    @DisplayName("유효한 테마 설명을 입력하면 객체가 생성된다.")
    @Test
    void createThemeName() {
        // Given
        final String value = "테바의 비밀친구는 누구인가?";

        // When
        final ThemeDescription themeDescription = new ThemeDescription(value);

        // Then
        assertThat(themeDescription).isNotNull();
    }

    @DisplayName("유효하지 않은 길이의 테마 설명이 입력되면 예외를 발생시킨다.")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {
            " ",
            "테바와 코딩을 하고 있으니 제가 LA에 있을 시절이 떠오르는................................................"
    })
    void themeDescriptionLengthTest(final String invalidDescription) {
        // When & Then
        assertThatThrownBy(() -> new ThemeDescription(invalidDescription))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("테마 설명은 1글자 이상 80글자 이하여야 합니다.");
    }
}
