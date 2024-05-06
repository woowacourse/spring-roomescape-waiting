package roomescape.domain.role;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class RoleTest {

    @ParameterizedTest
    @ValueSource(strings = {"admin", "member"})
    @DisplayName("역할의 이름으로 생성한다.")
    void fromRoleNameCreationTest(String roleName) {
        Role role = Role.from(roleName);
        assertThat(role.getRoleName()).isEqualTo(roleName);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"1", "admin1"})
    @DisplayName("역할의 이름이 잘못 입력된 경우 예외가 발생한다.")
    void fromRoleNameCreationExceptionTest(String roleName) {
        assertThatCode(() -> Role.from(roleName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당하는 역할이 없습니다.");
    }
}
