package roomescape.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import roomescape.security.authentication.Authentication;
import roomescape.security.provider.TokenProvider;
import roomescape.service.dto.request.CreateMemberRequest;
import roomescape.service.dto.request.CreateTokenRequest;
import roomescape.service.dto.response.TokenResponse;

class AuthServiceTest extends BaseServiceTest {

    private static final String EMAIL = "auth@gmail.com";
    private static final String PASSWORD = "password";
    private static final String NICKNAME = "nickname";

    @Autowired
    private AuthService authService;

    @Autowired
    private MemberService memberService;

    @SpyBean
    private TokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        CreateMemberRequest request = new CreateMemberRequest(EMAIL, PASSWORD, NICKNAME);
        memberService.createMember(request);
    }

    @Test
    @DisplayName("인증 후 토큰을 생성한다.")
    void createToken() {
        doReturn("created_token").when(tokenProvider).createToken(any());
        CreateTokenRequest request = new CreateTokenRequest(EMAIL, PASSWORD);

        TokenResponse response = authService.authenticateMember(request);

        assertThat(response.token()).isEqualTo("created_token");
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않을 경우 예외를 발생시킨다.")
    void validatePassword_fail_when_password_not_matched() {
        CreateTokenRequest request = new CreateTokenRequest(EMAIL, "wrong_password");

        assertThatThrownBy(() -> authService.authenticateMember(request))
                .isExactlyInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("등록되지 않은 이메일이거나 비밀번호가 틀렸습니다.");
    }

    @Test
    @DisplayName("토큰으로 인증 객체를 가져올 수 있다.")
    void getMemberId() {
        doReturn("1").when(tokenProvider).extractSubject(any());

        Authentication authentication = authService.createAuthentication("token");

        assertThat(authentication.getId()).isEqualTo(1L);
    }
}
