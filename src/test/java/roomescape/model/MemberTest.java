package roomescape.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.model.member.Member;
import roomescape.model.member.Role;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class MemberTest {

    @DisplayName("데이터의 id가 0 이하인 경우 예외를 발생시킨다.")
    @ParameterizedTest
    @ValueSource(longs = {0, -1, -999})
    void should_throw_exception_when_invalid_id(long id) {
        assertThatThrownBy(() -> new Member(id, "name", "email@woowahan.com", "password", Role.USER))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("id는 0 이하일 수 없습니다.");
    }

    @DisplayName("데이터의 이름이 null 혹은 비어있는 경우 예외를 발생시킨다.")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\n", "\t"})
    void should_throw_exception_when_invalid_name(String name) {
        assertThatThrownBy(() -> new Member(1L, name, "email@woowahan.com", "password", Role.USER))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("사용자 이름은 null 혹은 빈 문자열일 수 없습니다.");
    }

    @DisplayName("데이터의 이메일이 null 혹은 비어있는 경우 예외를 발생시킨다.")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\n", "\t"})
    void should_throw_exception_when_invalid_email(String email) {
        assertThatThrownBy(() -> new Member(1L, "name", email, "password", Role.USER))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이메일은 null 혹은 빈 문자열일 수 없습니다.");
    }

    @DisplayName("데이터의 이메일이 올바르지 않은 형태인 경우 예외를 발생시킨다.")
    @ParameterizedTest
    @ValueSource(strings = {"email", "email@", "email#woowahan.com"})
    void should_throw_exception_when_invalid_email_format(String email) {
        assertThatThrownBy(() -> new Member(1L, "name", email, "password", Role.USER))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("이메일은 올바른 형식이어야 합니다.");
    }

    @DisplayName("데이터의 비밀번호가 null 혹은 비어있는 경우 예외를 발생시킨다.")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\n", "\t"})
    void should_throw_exception_when_invalid_password(String password) {
        assertThatThrownBy(() -> new Member(1L, "name", "email@woowahan.com", password, Role.USER))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("비밀번호는 null 혹은 빈 문자열일 수 없습니다.");
    }
}
