package roomescape.member.dto;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.member.dto.request.SignupRequest;

class SignupRequestTest {

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("이메일 빈 값 에외 테스트")
    void email_exception(String email) {
        assertThatThrownBy(() -> new SignupRequest(email, "a", "a"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"a", "a.com", "aasd asd"})
    @DisplayName("이메일 형식 예외 테스트")
    void email_format_exception(String email) {
        assertThatThrownBy(() -> new SignupRequest(email, "a", "a"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("이름 빈 값 에외 테스트")
    void name_exception(String name) {
        assertThatThrownBy(() -> new SignupRequest("a@naver.com", name, "a"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("비밀번호 빈 값 에외 테스트")
    void password_exception(String password) {
        assertThatThrownBy(() -> new SignupRequest("a@naver.com", "a", password))
                .isInstanceOf(IllegalArgumentException.class);
    }
}