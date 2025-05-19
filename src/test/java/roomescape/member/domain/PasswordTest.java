package roomescape.member.domain;

import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.InvalidInputException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class PasswordTest {

    @Test
    void 비밀번호가_비어있거나_null이라면_예외가_발생한다() {
        // when & then
        SoftAssertions.assertSoftly(softAssertions -> {
            assertThatThrownBy(() -> Password.from(""))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("Password.value 은(는) 비어있을 수 없습니다.");

            assertThatThrownBy(() -> Password.from(null))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessage("Password.value 은(는) null일 수 없습니다.");
        });
    }
}
