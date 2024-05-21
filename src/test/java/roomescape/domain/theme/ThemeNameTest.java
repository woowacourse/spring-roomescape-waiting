package roomescape.domain.theme;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import roomescape.domain.member.MemberName;
import roomescape.exception.member.InvalidMemberNameLengthException;

class ThemeNameTest {
    @ParameterizedTest
    @CsvSource({"17", "18"})
    void 테마_이름_생성시_길이가_16자_이하가_아니면_예외가_발생한다(int length) {
        String name = "도".repeat(length);
        assertThatThrownBy(() -> new MemberName(name))
                .isInstanceOf(InvalidMemberNameLengthException.class);
    }
}
