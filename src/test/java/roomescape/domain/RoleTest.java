package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.member.Role;

class RoleTest {

    @DisplayName("문자열 MEMBER 로 열거형 Role을 반환한다.")
    @Test
    void return_enum_role_with_member() {
        Role member = Role.getRole("MEMBER");

        assertThat(member).isEqualByComparingTo(Role.MEMBER);
    }

    @DisplayName("문자열 ADMIN 로 열거형 Role을 반환한다.")
    @Test
    void return_enum_role_with_admin() {
        Role member = Role.getRole("ADMIN");

        assertThat(member).isEqualByComparingTo(Role.ADMIN);
    }

    @DisplayName("열거형 Role에 해당하는 문자열 입력이 아니면 예외를 발생시킨다..")
    @Test
    void throw_exception_when_invalid_role() {
        assertThatThrownBy(() -> Role.getRole("USER"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("회원의 권한이 존재하지 않습니다.");
    }

}
