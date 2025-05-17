package roomescape.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UserTest {

    @Test
    @DisplayName("사용자가 ADMIN인 지 알 수 있다.")
    void isAdmin() {
        var adminUser = new User(1L, new UserName("어드민"), UserRole.ADMIN, new Email("admin@email.com"), new Password("password"));
        var notAdminUser = new User(2L, new UserName("유저"), UserRole.USER, new Email("popo@email.com"), new Password("password"));

        assertAll(
            () -> assertThat(adminUser.isAdmin()).isTrue(),
            () -> assertThat(notAdminUser.isAdmin()).isFalse()
        );
    }
}
