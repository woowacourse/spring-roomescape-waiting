package roomescape.member.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class MemberEmailTest {

    @DisplayName("이메일 형식이 아니면 예외가 발생한다.")
    @ParameterizedTest
    @ValueSource(strings = {"abcdef", "aa@123", "email.com"})
    void testValidation(String email) {
        assertThatThrownBy(() -> new MemberEmail(email))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일 형식으로 입력해야 합니다.");
    }

    @DisplayName("빈 값이 입력되면 예외가 발생한다.")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "   "})
    void testValidationBlank(String email) {
        assertThatThrownBy(() -> new MemberEmail(email))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일 형식으로 입력해야 합니다.");
    }
}
