package roomescape.member.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RoleTest {

    @Test
    void 역할_이름으로_역할을_반환한다() {
        // Given
        String roleName = "Member";

        // When
        Role role = Role.from(roleName);

        // Then
        assertThat(role)
                .isEqualTo(Role.MEMBER);
    }

    @Test
    void 역할_이름이_소문자여도_정상적으로_반환한다() {
        // Given
        String roleName = "member";

        // When
        Role role = Role.from(roleName);

        // Then
        assertThat(role)
                .isEqualTo(Role.MEMBER);
    }

}
