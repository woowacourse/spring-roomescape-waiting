package roomescape.unit.reservation.domain.member;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import roomescape.member.domain.MemberName;

class MemberNameTest {

    @DisplayName("예약자 이름은 최소 2글자, 최대 5글자가 아니면 예외가 발생한다.")
    @ParameterizedTest
    @MethodSource("invalidNames")
    @NullAndEmptySource
    void validate(final String name) {
        // when & then
        assertThatThrownBy(() -> new MemberName(name))
                .isInstanceOf(IllegalArgumentException.class);
    }

    private static Stream<Arguments> invalidNames() {
        return Stream.of(
                Arguments.arguments(" "),
                Arguments.arguments("  "),
                Arguments.arguments("a".repeat(1)),
                Arguments.arguments("a".repeat(6))
        );
    }
}
