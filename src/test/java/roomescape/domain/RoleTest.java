package roomescape.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.domain.exception.RoleNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RoleTest {

    @Test
    @DisplayName("유효한 Role 반환")
    void validRole() {
        final Role admin = Role.findByName("ADMIN");
        final Role user = Role.findByName("USER");

        assertThat(admin).isEqualTo(Role.ADMIN);
        assertThat(user).isEqualTo(Role.USER);
    }

    @Test
    @DisplayName("유효하지 않은 role일시 예외 발생")
    void invalidRole() {
        assertThatThrownBy(() -> Role.findByName("red"))
                .isInstanceOf(RoleNotFoundException.class);
    }
}
