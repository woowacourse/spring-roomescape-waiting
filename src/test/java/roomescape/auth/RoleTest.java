package roomescape.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class RoleTest {

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
