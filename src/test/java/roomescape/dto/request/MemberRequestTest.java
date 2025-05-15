package roomescape.dto.request;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.controller.dto.request.MemberRequest;
import roomescape.exception.custom.InvalidInputException;

class MemberRequestTest {

    @Test
    @DisplayName("이름은 빈 값이 들어올 수 없다.")
    void validateNullOfName() {
        assertThatThrownBy(() -> new MemberRequest(null, "이메일", "비밀번호"))
            .isInstanceOf(InvalidInputException.class)
            .hasMessageContaining("선택되지 않은 값 존재");
    }

    @Test
    @DisplayName("이메일은 빈 값이 들어올 수 없다.")
    void validateNullOfEmail() {
        assertThatThrownBy(() -> new MemberRequest("이름", null, "비밀번호"))
            .isInstanceOf(InvalidInputException.class)
            .hasMessageContaining("선택되지 않은 값 존재");
    }

    @Test
    @DisplayName("비밀번호는 빈 값이 들어올 수 없다.")
    void validateNullOfPassword() {
        assertThatThrownBy(() -> new MemberRequest("이름", "이메일", null))
            .isInstanceOf(InvalidInputException.class)
            .hasMessageContaining("선택되지 않은 값 존재");
    }

    @Test
    @DisplayName("이름은 한 글자 이상이어야 한다")
    void validateLengthOfName() {
        assertThatThrownBy(() -> new MemberRequest("", "이메일", "비밀번호"))
            .isInstanceOf(InvalidInputException.class)
            .hasMessageContaining("입력되지 않은 값 존재");
    }

    @Test
    @DisplayName("이메일은 한 글자 이상이어야 한다")
    void validateLengthOfEmail() {
        assertThatThrownBy(() -> new MemberRequest("이름", "", "비밀번호"))
            .isInstanceOf(InvalidInputException.class)
            .hasMessageContaining("입력되지 않은 값 존재");
    }

    @Test
    @DisplayName("비밀번호는 한 글자 이상이어야 한다")
    void validateLengthOfPassword() {
        assertThatThrownBy(() -> new MemberRequest("이름", "이메일", ""))
            .isInstanceOf(InvalidInputException.class)
            .hasMessageContaining("입력되지 않은 값 존재");
    }
}
