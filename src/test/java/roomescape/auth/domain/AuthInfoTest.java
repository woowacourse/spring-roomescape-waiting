package roomescape.auth.domain;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.member.domain.MemberRole;

class AuthInfoTest {

    @Test
    @DisplayName("관리자가 아닌 경우 참을 반환한다.")
    void isNotAdmin() {
        assertTrue(MemberRole.USER.isNotAdmin());
    }

    @Test
    @DisplayName("관리자인 경우 거짓을 반환한다.")
    void isAdmin() {
        assertFalse(MemberRole.ADMIN.isNotAdmin());
    }
}
