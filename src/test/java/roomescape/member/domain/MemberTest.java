package roomescape.member.domain;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import roomescape.global.exception.InvalidArgumentException;

class MemberTest {

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"@naver.com", "213213123", "rizmsfns@", "@"})
    void 이메일_형식이_아니면_예외를_던진다(String email) {
        Password password = Mockito.mock(Password.class);

        assertThatThrownBy(() -> Member.signUpUser("user", email, password))
                .isInstanceOf(InvalidArgumentException.class);
    }

    @Test
    void 생성_테스트() {
        Password password = new Password("1234");

        assertThatCode(() -> Member.signUpUser("꾹", "admin@naver.com", password))
                .doesNotThrowAnyException();
    }

}
