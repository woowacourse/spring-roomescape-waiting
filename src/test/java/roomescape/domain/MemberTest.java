package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.domain.member.Member;
import roomescape.domain.member.Role;

class MemberTest {

    @Test
    @DisplayName("회원을 생성한다.")
    void create() {
        assertThatCode(() -> new Member("example@gmail.com", "abc123", "구름", Role.USER))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @DisplayName("이메일이 공백이면 예외가 발생한다.")
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void validateEmail(String email) {
        assertThatThrownBy(() -> new Member(email, "abc123", "구름", Role.USER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일은 필수 값입니다.");
    }

    @Test
    @DisplayName("이메일이 255자를 넘으면 예외가 발생한다.")
    void validateEmailLength() {
        String email = "a".repeat(256);

        assertThatThrownBy(() -> new Member(email, "abc123", "구름", Role.USER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이메일은 255자를 넘을 수 없습니다.");
    }

    @ParameterizedTest
    @DisplayName("비밀번호가 공백이면 예외가 발생한다.")
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void validatePassword(String password) {
        assertThatThrownBy(() -> new Member("example@gmail.com", password, "구름", Role.USER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호는 필수 값입니다.");
    }

    @Test
    @DisplayName("비밀번호가 255자를 넘으면 예외가 발생한다.")
    void validatePasswordLength() {
        String password = "a".repeat(256);

        assertThatThrownBy(() -> new Member("example@gmail.com", password, "구름", Role.USER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("비밀번호는 255자를 넘을 수 없습니다.");
    }

    @ParameterizedTest
    @DisplayName("이름이 공백이면 예외가 발생한다.")
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void validateName(String name) {
        assertThatThrownBy(() -> new Member("example@gmail.com", "abc123", name, Role.USER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이름은 필수 값입니다.");
    }

    @Test
    @DisplayName("이름이 30자를 넘으면 예외가 발생한다.")
    void validateNameLength() {
        String name = "a".repeat(31);

        assertThatThrownBy(() -> new Member("example@gmail.com", "abc123", name, Role.USER))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이름은 30자를 넘을 수 없습니다.");
    }

    @Test
    @DisplayName("역할이 없으면 예외가 발생한다.")
    void validateRole() {
        assertThatThrownBy(() -> new Member("example@gmail.com", "abc123", "구름", null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("역할은 필수 값입니다.");
    }
}
