package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import roomescape.exception.member.InvalidMemberEmailPatternException;

class MemberEmailTest {
    @ParameterizedTest
    @CsvSource({"admin@gmail.kr", "h@a.kr"})
    void 이메일을_생성할_수_있다(String address) {
        assertThatCode(() -> new MemberEmail(address))
                .doesNotThrowAnyException();
    }

    @ParameterizedTest
    @CsvSource({"admin", "admin@", "admin@gmail", "admin@gmail.k", "@gmail.kr"})
    void 이메일_생성시_형식이_틀리면_예외가_발생한다(String address) {
        assertThatThrownBy(() -> new MemberEmail(address))
                .isInstanceOf(InvalidMemberEmailPatternException.class);
    }
}
