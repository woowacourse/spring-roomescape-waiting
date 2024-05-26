package roomescape.domain;

import static roomescape.TestFixture.ADMIN_EMAIL;
import static roomescape.TestFixture.ADMIN_NAME;
import static roomescape.TestFixture.ADMIN_PASSWORD;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.exception.BadRequestException;

class MemberTest {

    @DisplayName("이름에 null 혹은 빈문자열이 들어가면 예외를 발생시킨다.")
    @ParameterizedTest
    @NullAndEmptySource
    void nullEmptyName(String value) {
        Assertions.assertThatThrownBy(() -> new Member(value, ADMIN_EMAIL, ADMIN_PASSWORD, Role.ADMIN))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("이름에 빈값을 입력할 수 없습니다.");
    }

    @DisplayName("설명에 null이 들어가면 예외를 발생시킨다.")
    @ParameterizedTest
    @NullSource
    void nullEmail(String value) {
        Assertions.assertThatThrownBy(() -> new Member(ADMIN_NAME, value, ADMIN_PASSWORD, Role.ADMIN))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("이메일에 빈값을 입력할 수 없습니다.");
    }

    @DisplayName("이메일 형식이 아니면 예외를 발생시킨다.")
    @ParameterizedTest
    @ValueSource(strings = {"zeze", "zeze@", "@gmail.com"})
    void invalidEmailPattern(String value) {
        Assertions.assertThatThrownBy(() -> new Member(ADMIN_NAME, value, ADMIN_PASSWORD, Role.ADMIN))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("이메일 형식이 올바르지 않습니다.");
    }

    @DisplayName("비밀번호에 null 혹은 빈문자열이 들어가면 예외를 발생시킨다.")
    @ParameterizedTest
    @NullAndEmptySource
    void nullEmptyPassword(String value) {
        Assertions.assertThatThrownBy(() -> new Member(ADMIN_NAME, ADMIN_EMAIL, value, Role.ADMIN))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("비밀번호에 빈값을 입력할 수 없습니다.");
    }

    @DisplayName("역할에 null이 들어가면 예외를 발생시킨다.")
    @ParameterizedTest
    @NullSource
    void nullRole(Role value) {
        Assertions.assertThatThrownBy(() -> new Member(ADMIN_NAME, ADMIN_EMAIL, ADMIN_PASSWORD, value))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("역할에 빈값을 입력할 수 없습니다.");
    }
}
