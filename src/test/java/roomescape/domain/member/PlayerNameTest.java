package roomescape.domain.member;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class PlayerNameTest {

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("이름이 비어있거나 null이면 예외가 발생한다.")
    void shouldThrowExceptionWhenNameIsNullOrEmpty(String input) {
        assertThatCode(() -> new PlayerName(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약자명은 필수 입력값 입니다.");
    }

    @Test
    @DisplayName("이름이 20자를 초과하면 예외가 발생한다.")
    void shouldThrowExceptionWhenNameLengthExceededMaxLength() {
        String input = "-".repeat(21);
        assertThatCode(() -> new PlayerName(input))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"1", "12345678901234567890"})
    @DisplayName("이름이 올바르게 생성된다.")
    void shouldCreateName(String input) {
        assertThatCode(() -> new PlayerName(input))
                .doesNotThrowAnyException();
    }
}
