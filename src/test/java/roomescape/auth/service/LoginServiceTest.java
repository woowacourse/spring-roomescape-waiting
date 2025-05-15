package roomescape.auth.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.auth.dto.LoginRequest;
import roomescape.auth.dto.TokenResponse;
import roomescape.exception.InvalidAuthorizationException;
import roomescape.fixture.FakeMemberRepositoryFixture;
import roomescape.member.dto.MemberResponse;
import roomescape.member.repository.MemberRepository;
import roomescape.member.service.MemberService;
import roomescape.repository.FakeTokenProvider;
import roomescape.util.TokenProvider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("사용자 로그인")
class LoginServiceTest {

    private final MemberRepository memberRepository = FakeMemberRepositoryFixture.create();
    private final TokenProvider tokenProvider = new FakeTokenProvider();
    private final LoginService loginService = new LoginService(memberRepository, new MemberService(memberRepository, tokenProvider), tokenProvider);

    @DisplayName("올바른 사용자 정보를 전달하면 로그인 토큰을 생성한다")
    @Test
    void createTokenTest() {
        // given
        LoginRequest request = new LoginRequest("wooteco7", "admin@gmail.com");

        // when
        TokenResponse token = loginService.createToken(request);
        String expected = "admin@gmail.com";

        // then
        assertThat(token.accessToken()).isEqualTo(expected);
    }

    @DisplayName("토큰 정보로 사용자 응답 DTO를 추출할 수 있다")
    @Test
    void findMemberTest() {
        // given
        String token = "admin@gmail.com";

        // when
        MemberResponse response = loginService.findMemberByToken(token);

        // then
        assertAll(
                () -> assertThat(response.id()).isEqualTo(1L),
                () -> assertThat(response.name()).isEqualTo("어드민")
        );
    }

    @DisplayName("토큰 생성 시 사용자 정보가 잘못되면 예외가 발생한다")
    @Test
    void createTokenExceptionTest() {
        // given
        LoginRequest request = new LoginRequest("", "admin@gmail.com");

        // when & then
        assertThatThrownBy(() -> loginService.createToken(request)).isInstanceOf(InvalidAuthorizationException.class);
    }
}
