package roomescape.unit.domain.member;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.domain.member.MemberPassword;

class MemberPasswordTest {

    @Test
    void 비밀번호는_null일_수_없다() {
        // when // then
        assertThatThrownBy(() -> new MemberPassword(null))
                .isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "1234567890123456789012345678902",
            "12345678901234567890123456789023",
    })
    void 비밀번호가_30자를_초과하면_예외가_발생한다(String password) {
        // when // then
        assertThatThrownBy(() -> new MemberPassword(password))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "gustn3!", // 숫자가 2자 이상 포함되지 않은 경우
            "gustn33", // 특수문자가 1자 이상 포함되지 않은 경우
            "gus33!" // 영문자가 5자 이상 포함되지 않은 경우
    })
    void 올바르지_않은_형식의_비밀번호는_예외를_발생한다(String password) {
        // when // then
        assertThatThrownBy(() -> new MemberPassword(password))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = { // 영문 5자 이상 and 숫자 2자 이상 and 특수문자 1자 이상 포함
            "gustn33!",
            "gustn33333@#@#@"
    })
    void 비밀번호를_올바르게_생성한다(String password) {
        // when // then
        assertThatCode(() -> new MemberPassword(password))
                .doesNotThrowAnyException();
    }
}
