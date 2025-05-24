package roomescape.unit.domain.member;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import roomescape.domain.member.MemberEmail;

class MemberEmailTest {

    @Test
    void 이메일은_null일_수_없다() {
        // when // then
        assertThatThrownBy(() -> new MemberEmail(null))
                .isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "leehyeonsu%gmail.com",
            "leehyeonsugmail.com",
            "leehyeonsu@gmailcom",
            "@gmail.com",
            "nsu.gmail.com.com",
            "nsu.gmail.com.@com",
    })
    void 이메일_형식이_아니면_예외가_발생한다(String email) {
        // when // then
        assertThatThrownBy(() -> new MemberEmail(email))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "12345678901234567890123456789012345678901",
            "123456789012345678901234567890123456789012",
    })
    void 이메일이_40자_초과면_예외가_발생한다(String email) {
        // when // then
        assertThatThrownBy(() -> new MemberEmail(email))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "leehyeonsu@gmail.com",
            "leehyeons.u@gmail.com",
            "2222222222222222222leehyeons.u@gmail.com"
    })
    void 이메일을_올바르게_생성한다(String email) {
        // when // then
        assertThatCode(() -> new MemberEmail(email))
                .doesNotThrowAnyException();
    }
}
