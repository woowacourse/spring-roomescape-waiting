package roomescape.model;

import java.util.stream.Stream;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class RoleTest {
    private static Stream<Arguments> testCasesForFindingRoleByValue() {
        return Stream.of(
                Arguments.of("ADMIN", Role.ADMIN),
                Arguments.of("USER", Role.USER)
        );
    }

    @ParameterizedTest
    @MethodSource("testCasesForFindingRoleByValue")
    @DisplayName("값을 이용해 Role 을 찾는다")
    void test(String value, Role expected) {
        // when
        Role role = Role.fromValue(value);

        // then
        assertThat(role).isEqualTo(expected);
    }
}
