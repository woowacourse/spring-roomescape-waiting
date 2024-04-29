package roomescape.dto;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.dto.exception.InputNotAllowedException;

class InputValidatorTest {

    @DisplayName("null 값이 들어올 경우 예외로 처리한다.")
    @Test
    void validateNotNull() {
        Integer arg = null;
        Assertions.assertThatThrownBy(() -> InputValidator.validateNotNull(arg))
                .isInstanceOf(InputNotAllowedException.class)
                .hasMessage("입력 형식이 올바르지 않습니다. - null은 입력할 수 없습니다.");
    }

    @DisplayName("빈 문자열이 들어올 경우 예외로 처리한다.")
    @Test
    void validateNotEmpty() {
        Assertions.assertThatThrownBy(() -> InputValidator.validateNotBlank(""))
                .isInstanceOf(InputNotAllowedException.class)
                .hasMessage("입력 형식이 올바르지 않습니다. - 빈 문자열은 입력할 수 없습니다.");
    }
}
