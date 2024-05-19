package roomescape.domain.member.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RoleTest {

    @DisplayName("역할의 이름으로 역할객체를 찾을 수 있다.")
    @Test
    void should_find_role_object_by_role_string() {
        Role expectedRole = Role.ADMIN;

        Role actualRole = Role.convertToRole("admin");

        assertThat(actualRole).isEqualTo(expectedRole);
    }
}
