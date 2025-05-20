package roomescape.user.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class UserRoleTest {

    @Test
    @DisplayName("관리자는 일반 유저의 권한을 포함한다")
    void includes() {
        // when
        // then
        assertThat(UserRole.ADMIN.includes(UserRole.NORMAL)).isTrue();
    }
}
