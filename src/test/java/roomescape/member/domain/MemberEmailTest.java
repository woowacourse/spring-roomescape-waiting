package roomescape.member.domain;

import org.junit.jupiter.api.Test;
import roomescape.common.exception.InvalidInputException;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberEmailTest {

    @Test
    void 이메일_형식이_아니라면_예외가_발생한다() {
        // Given
        final String email = "123.com";

        // When & Then
        assertThatThrownBy(() -> MemberEmail.from(email)
        ).isInstanceOf(InvalidInputException.class);
    }

    @Test
    void 이메일이_null이라면_예외가_발생한다() {
        // When & Then
        assertThatThrownBy(() -> MemberEmail.from(null)
        ).isInstanceOf(InvalidInputException.class);
    }

    @Test
    void 이메일_객체가_생성된다() {
        // Given
        final String email = "123@gamil.com";

        // When & Then
        assertThatNoException().isThrownBy(() -> MemberEmail.from(email));
    }
}
