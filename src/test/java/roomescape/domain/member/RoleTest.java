package roomescape.domain.member;

import static org.assertj.core.api.Assertions.assertThat;
import static roomescape.TestFixture.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class RoleTest {

    @ParameterizedTest
    @MethodSource("memberAndExpected")
    @DisplayName("사용자가 어드민이 아닌지 확인한다.")
    void checkIsAdmin(final Member member, final boolean expected) {
        // given
        final Role role = member.getRole();

        // when
        final boolean actual = Role.isNotAdmin(role);

        // then
        assertThat(actual).isEqualTo(expected);
    }

    private static Stream<Arguments> memberAndExpected() {
        return Stream.of(
                Arguments.of(ADMIN(), false),
                Arguments.of(MEMBER_MIA(), true)
        );
    }
}
