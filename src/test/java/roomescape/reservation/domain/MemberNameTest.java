package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.member.domain.MemberName;

class MemberNameTest {

    @DisplayName("예약자명은 최소 1글자, 최대 5글자가 아니면 예외가 발생한다.")
    @ParameterizedTest
    @ValueSource(strings = {"aaaaaa", " ", "   "})
    @NullAndEmptySource
    void testValidateName(String name) {
        // when
        // then
        assertThatThrownBy(() -> new MemberName(name))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("예약자명은 최소 1글자, 최대 5글자여야합니다.");
    }
}
