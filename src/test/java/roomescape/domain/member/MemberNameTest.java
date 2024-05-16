package roomescape.domain.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.exception.InvalidReservationException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class MemberNameTest {
    @DisplayName("이름은 1자 미만, 20자 초과일 경우 예외를 발생시킨다.")
    @ParameterizedTest
    @EmptySource
    @ValueSource(strings = {"linirinilinirinilinirinilinirini"})
    void invalidNameLength(String name) {
        assertThatThrownBy(() -> new Member(name, "lini@email.com", "lini123", Role.GUEST))
                .isInstanceOf(InvalidReservationException.class)
                .hasMessage("이름은 1자 이상, 20자 이하여야 합니다.");
    }
}
