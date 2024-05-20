package roomescape.auth.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RoleTest {

    @DisplayName("ADMIN 권한 인지 확인한다.")
    @Test
    void isAdmin() {
        Role admin = Role.ADMIN;
        Role member = Role.MEMBER;

        assertAll(
                () -> assertThat(admin.isAdmin()).isTrue(),
                () -> assertThat(member.isAdmin()).isFalse()
        );
    }
}
