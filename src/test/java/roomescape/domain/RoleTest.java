package roomescape.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoleTest {
    @DisplayName("현재 역할이 관리자 권한인지 확인할 수 있다.")
    @Test
    void given_adminRole_when_isAdmin_then_true() {
        //given
        Role adminRole = Role.ADMIN;
        //when
        boolean actual = adminRole.isAdmin();
        //then
        assertThat(actual).isTrue();
    }
}
