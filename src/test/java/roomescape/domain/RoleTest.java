package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RoleTest {
    @DisplayName("현재 역할이 관리자 권한인지 확인할 수 있다.")
    @Test
    void given_adminRole_when_isAdmin_then_isTrue() {
        //given
        Role adminRole = Role.ADMIN;
        //when, then
        assertThat(adminRole.isAdmin()).isTrue();
    }
}
