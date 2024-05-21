package roomescape.domain;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class RoleTest {

    @ParameterizedTest
    @EnumSource(Role.class)
    @DisplayName("텍스트를 통해 권한 객체를 생성한다.")
    void of_ShouldGenerateObject(Role role) {
        // given
        String value = role.getValue();

        // when
        Role generatedRole = Role.of(value);

        // then
        Assertions.assertThat(generatedRole).isSameAs(role);
    }

}
