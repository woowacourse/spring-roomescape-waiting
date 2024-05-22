package roomescape.domain.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.TestFixture;
import roomescape.exception.AuthorizationException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemberTest {

    @Test
    @DisplayName("두 비밀번호가 일치하면 예외가 발생하지 않는다.")
    void checkIncorrectPassword() {
        // given
        final Member member = TestFixture.MEMBER_CAT();
        final String password = "1234";

        // when & then
        assertThatCode(() -> member.checkIncorrectPassword(password))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("두 비밀번호가 일치하지 않으면 예외가 발생한다.")
    void throwExceptionWhenPasswordMismatched() {
        // given
        final Member member = TestFixture.MEMBER_CAT();
        final String inCorrectPassword = "1233";

        // when & then
        assertThatThrownBy(() -> member.checkIncorrectPassword(inCorrectPassword))
                .isInstanceOf(AuthorizationException.class);
    }
}
