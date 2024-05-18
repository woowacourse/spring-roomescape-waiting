package roomescape.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import roomescape.exception.member.InvalidMemberNameLengthException;

class MemberNameTest {
    @ParameterizedTest
    @CsvSource({"안", "안녕하세요오"})
    void 이름_생성시__길이가_2자_이상_5자_이하가_아니면_예외가_발생한다(String name) {
        assertThatThrownBy(() -> new MemberName(name))
                .isInstanceOf(InvalidMemberNameLengthException.class);
    }
}
