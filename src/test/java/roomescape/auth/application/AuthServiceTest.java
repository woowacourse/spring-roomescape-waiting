package roomescape.auth.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.auth.exception.AuthorizationException;
import roomescape.common.ServiceTest;
import roomescape.member.application.MemberService;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.TestFixture.*;

class AuthServiceTest extends ServiceTest {
    @Autowired
    private AuthService authService;

    @Autowired
    private MemberService memberService;

    @Test
    @DisplayName("실제 사용자의 비밀번호와 입력 비밀번호가 일치하지 않으면 토큰을 생성할 수 없다.")
    void createTokenWithInvalidEmail() {
        // given
        memberService.create(USER_MIA());
        String invalidPassword = "invalid";

        // when & then
        assertThatThrownBy(() -> authService.createToken(MIA_EMAIL, invalidPassword))
                .isInstanceOf(AuthorizationException.class);
    }

    @Test
    @DisplayName("이메일을 가진 사용자가 존재하지 않으면 토큰을 생성할 수 없다.")
    void findByEmail() {
        // given
        String notExistingEmail = "notExistingEmail@google.com";

        // when & then
        assertThatThrownBy(() -> authService.createToken(notExistingEmail, TEST_PASSWORD))
                .isInstanceOf(AuthorizationException.class);
    }
}
