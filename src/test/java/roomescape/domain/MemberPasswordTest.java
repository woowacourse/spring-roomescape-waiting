package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import roomescape.exception.member.InvalidMemberPasswordLengthException;

class MemberPasswordTest {
    @ParameterizedTest
    @CsvSource({"7", "17"})
    void 비밀번호_생성시__길이가_8자_이상_16자_이하가_아니면_예외가_발생한다(int length) {
        String password = "p".repeat(length);
        assertThatThrownBy(() -> new MemberPassword(password))
                .isInstanceOf(InvalidMemberPasswordLengthException.class);
    }
}
