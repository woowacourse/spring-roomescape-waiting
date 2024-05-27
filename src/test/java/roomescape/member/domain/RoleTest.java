package roomescape.member.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RoleTest {

    @Test
    @DisplayName("문자열과 동일한 권한 Enum 반환")
    void findMemberRole() {
        assertThat(Role.valueOf("USER")).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("회원의 권한이 관리자가 아님: 참")
    void isNotAdmin() {
        assertTrue(Role.USER.isNotAdmin());
    }

    @Test
    @DisplayName("회원의 권한이 관리자임: 거짓")
    void isAdmin() {
        assertFalse(Role.ADMIN.isNotAdmin());
    }
}
