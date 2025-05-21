package roomescape.member.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class MemberNameTest {

    @DisplayName("예약자명은 최소 1글자, 최대 5글자가 아니면 예외가 발생한다.")
    @ParameterizedTest
    @ValueSource(strings = {"abcdef", "가나다라마바"})
    @NullAndEmptySource
    void testValidateName(String name) {
        assertThatThrownBy(() -> new MemberName(name))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용자명은 최소 1글자, 최대 5글자여야합니다.");
    }

    @DisplayName("빈 값이 입력되면 예외가 발생한다.")
    @ParameterizedTest
    @ValueSource(strings = {" ", "   "})
    @NullAndEmptySource
    void testValidateNameBlank(String name) {
        assertThatThrownBy(() -> new MemberName(name))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("사용자명은 최소 1글자, 최대 5글자여야합니다.");
    }
}
