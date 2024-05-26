package roomescape.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.BasicAcceptanceTest;
import roomescape.dto.TokenRequest;
import roomescape.exception.RoomescapeException;
import roomescape.infrastructure.auth.JwtTokenManager;
import roomescape.infrastructure.auth.JwtTokenProperties;

class AuthServiceTest extends BasicAcceptanceTest {
    @Autowired
    private AuthService authService;

    @Autowired
    private JwtTokenProperties jwtTokenProperties;

    @DisplayName("존재하지 않는 email을 입력 시 예외를 발생시킨다.")
    @Test
    void invalidNotExistEmail() {
        TokenRequest tokenRequest = new TokenRequest("notExist@wooteco.com", "wootecoCrew6!");

        assertThatThrownBy(() -> authService.createToken(tokenRequest))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(String.format("존재하지 않는 회원입니다. 입력한 회원 email:%s", tokenRequest.email()));
    }

    @DisplayName("email에 해당하지 않는 password를 입력 시 예외를 발생시킨다.")
    @Test
    void invalidNotEqualsPassword() {
        TokenRequest tokenRequest = new TokenRequest("gomding@wooteco.com", "dffd@efg32");

        assertThatThrownBy(() -> authService.createToken(tokenRequest))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage(String.format("존재하지 않는 회원입니다. 입력한 회원 email:%s", tokenRequest.email()));
    }

    @DisplayName("유효기간이 만료된 토큰일 시 예외를 발생시킨다.")
    @Test
    void validateToken() {
        jwtTokenProperties.setExpireMilliseconds(-1L);
        JwtTokenManager jwtTokenManager = new JwtTokenManager(jwtTokenProperties);
        String token = jwtTokenManager.createToken("1");
        Cookie cookie = jwtTokenManager.addTokenToCookie(token);
        Cookie[] cookies = new Cookie[]{cookie};

        assertThatThrownBy(() -> authService.validateToken(cookies))
                .isInstanceOf(RoomescapeException.class)
                .hasMessage("인증 유효기간이 만료되었습니다.");
    }
}
