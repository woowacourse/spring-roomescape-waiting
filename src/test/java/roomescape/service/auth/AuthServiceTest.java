package roomescape.service.auth;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import roomescape.DBTest;
import roomescape.exception.NotFoundException;
import roomescape.repository.MemberRepository;
import roomescape.service.dto.request.LoginRequest;

@Import(JwtConfig.class)
class AuthServiceTest extends DBTest {

    @Autowired
    private AuthService authService;

    @DisplayName("존재하지 않는 아이디, 비밀번호로 로그인시 예외가 발생한다.")
    @Test
    void login() {
        // given
        String id = "not_exist_id@email.com";
        String password = "not_exist_password";

        // when & then
        assertThatThrownBy(() -> authService.login(new LoginRequest(id, password)))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("일치하는 회원 정보가 없습니다. email = " + id + ", password = " + password);
    }

    @TestConfiguration
    static class AuthServiceTestConfig {

        @Autowired
        public AuthService authService(MemberRepository memberRepository, JwtTokenManager jwtTokenManager,
                                       TokenCookieManager tokenCookieManager) {
            return new AuthService(memberRepository, jwtTokenManager, tokenCookieManager);
        }
    }
}
