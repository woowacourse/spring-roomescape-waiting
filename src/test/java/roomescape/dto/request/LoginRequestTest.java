package roomescape.dto.request;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.controller.dto.request.LoginRequest;
import roomescape.exception.custom.InvalidInputException;

class LoginRequestTest {

    @Test
    @DisplayName("이메일은 빈 값이 들어올 수 없다.")
    void validateNullOfEmail() {
        assertThatThrownBy(() -> new LoginRequest(null, "비밀번호"))
            .isInstanceOf(InvalidInputException.class)
            .hasMessageContaining("선택되지 않은 값 존재");
    }

    @Test
    @DisplayName("비밀번호는 빈 값이 들어올 수 없다.")
    void validateNullOfPassword() {
        assertThatThrownBy(() -> new LoginRequest("이메일", null))
            .isInstanceOf(InvalidInputException.class)
            .hasMessageContaining("선택되지 않은 값 존재");
    }

    @Test
    @DisplayName("이메일은 한 글자 이상이어야 한다")
    void validateLengthOfEmail() {
        assertThatThrownBy(() -> new LoginRequest("", "비밀번호"))
            .isInstanceOf(InvalidInputException.class)
            .hasMessageContaining("입력되지 않은 값 존재");
    }

    @Test
    @DisplayName("비밀번호는 한 글자 이상이어야 한다")
    void validateLengthOfPassword() {
        assertThatThrownBy(() -> new LoginRequest("이메일", ""))
            .isInstanceOf(InvalidInputException.class)
            .hasMessageContaining("입력되지 않은 값 존재");
    }
}
