package roomescape.member.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RoleTest {

    @Test
    @DisplayName("문자열로부터, 해당하는 권한 Enum을 반환한다.")
    void findMemberRole() {
        assertThat(Role.getMemberRole("USER")).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("회원의 권한이 관리자가 아닌 경우, 참을 반환한다.")
    void isNotAdmin() {
        assertTrue(Role.USER.isNotAdmin());
    }

    @Test
    @DisplayName("회원의 권한이 관리자인 경우, 거짓을 반환한다.")
    void isAdmin() {
        assertFalse(Role.ADMIN.isNotAdmin());
    }
}
