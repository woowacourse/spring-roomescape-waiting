package roomescape.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import jakarta.servlet.http.Cookie;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import roomescape.auth.dto.LoggedInMember;
import roomescape.auth.dto.LoginRequest;
import roomescape.auth.dto.RequestCookies;
import roomescape.member.dao.MemberDao;
import roomescape.member.domain.Member;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    private static final String TEST_SECRET_KEY = "this-is-test-secret-key-this-is-test-secret-key";

    @Mock
    private MemberDao memberDao;
    private TokenProvider tokenProvider = new TokenProvider(TEST_SECRET_KEY);
    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(tokenProvider, memberDao);
    }

    @DisplayName("토큰 생성 시, 해당 멤버가 없을 경우 예외를 던진다.")
    @Test
    void createTokenTest_whenMemberNotExist() {
        LoginRequest request = new LoginRequest("not_exist@abc.com", "1234");
        given(memberDao.findMemberByEmail("not_exist@abc.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> authService.createToken(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 멤버가 존재하지 않습니다.");
    }

    @DisplayName("해당 토큰의 유저를 찾을 수 있다.")
    @Test
    void findLoggedInMemberTest() {
        String token = makeToken("브리", "bri@abc.com");
        given(memberDao.findMemberById(1L)).willReturn(Optional.of(new Member(1L, "브리", "bri@abc.com")));
        LoggedInMember expected = new LoggedInMember(1L, "브리", "bri@abc.com", false);

        LoggedInMember actual = authService.findLoggedInMember(token);

        assertThat(actual).isEqualTo(expected);
    }

    private String makeToken(String name, String email) {
        LoginRequest request = new LoginRequest(email, "1234");
        given(memberDao.findMemberByEmail(email))
                .willReturn(Optional.of(new Member(1L, name, email)));
        return authService.createToken(request);
    }
}
