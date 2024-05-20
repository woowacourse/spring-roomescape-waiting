package roomescape.domain.member.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.global.exception.EscapeApplicationException;

class RoleTest {

    @DisplayName("역할의 이름으로 역할객체를 찾을 수 있다.")
    @Test
    void should_find_role_object_by_role_string() {
        Role expectedRole = Role.ADMIN;

        Role actualRole = Role.convertToRole("admin");

        assertThat(actualRole).isEqualTo(expectedRole);
    }

    @DisplayName("잘못된 역할의 이름으로 역할객체를 찾는 경우 예외가 발생한다.")
    @Test
    void should_throw_exception_when_convert_with_wrong_role_name() {
        assertThatThrownBy(() -> Role.convertToRole("wrongRole"))
                .isInstanceOf(EscapeApplicationException.class)
                .hasMessage("존재하지 않는 역할입니다.");
    }
}
