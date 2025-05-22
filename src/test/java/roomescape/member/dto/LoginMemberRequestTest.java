package roomescape.member.dto;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import roomescape.member.dto.request.LoginRequest;

class LoginMemberRequestTest {

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("이메일 빈 값 에외 테스트")
    void email_exception(String email) {
        assertThatThrownBy(() -> new LoginRequest(email, "a"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("비밀번호 빈 값 에외 테스트")
    void password_exception(String password) {
        assertThatThrownBy(() -> new LoginRequest("a@naver.com", password))
                .isInstanceOf(IllegalArgumentException.class);
    }

}