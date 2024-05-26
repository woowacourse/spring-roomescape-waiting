package roomescape.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.auth.core.token.TokenProvider;
import roomescape.auth.domain.AuthInfo;
import roomescape.auth.dto.request.LoginRequest;
import roomescape.auth.dto.response.GetAuthInfoResponse;
import roomescape.auth.dto.response.LoginResponse;
import roomescape.fixture.MemberFixture;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;
import roomescape.util.ServiceTest;

class AuthServiceTest extends ServiceTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private AuthService authService;

    @Test
    @DisplayName("로그인 성공 시, 해당하는 회원의 정보를 담은 토큰을 반환한다. ")
    void login() {
        String email = "asdf@naver.com";
        Member member = memberRepository.save(MemberFixture.getOne(email));

        LoginRequest loginRequest = new LoginRequest(email, member.getPassword());

        // when
        LoginResponse loginResponse = authService.login(loginRequest);

        // then
        assertThat(tokenProvider.extractAuthInfo(loginResponse.token()).getName())
                .isEqualTo(member.getName());
    }

    @Test
    @DisplayName("로그인 시 비밀번호가 올바르지 않는 경우, 예외를 반환한다.")
    void login_WhenNotSamePassword() {
        // given
        String email = "asdf@naver.com";
        Member member = memberRepository.save(MemberFixture.getOne(email));
        LoginRequest loginRequest = new LoginRequest(email, member.getPassword() + "asdf");

        // when & then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("아이디 또는 비밀번호를 잘못 입력했습니다. 다시 입력해주세요.");
    }

    @Test
    @DisplayName("회원 정보를 조회한다.")
    void getMemberAuthInfo() {
        // given
        Member member = memberRepository.save(MemberFixture.getOne());
        AuthInfo authInfo = new AuthInfo(member.getId(), member.getName(), member.getMemberRole());

        // when & then
        assertThat(authService.getMemberAuthInfo(authInfo))
                .isEqualTo(new GetAuthInfoResponse(authInfo.getName()));
    }

    @Test
    @DisplayName("회원 정보 조회 시, 해당하는 회원이 없는 경우 예외를 반환한다.")
    void getMemberAuthInfo_WhenMemberNotExist() {
        // given
        Member member = MemberFixture.getOneWithId(1L);
        AuthInfo authInfo = new AuthInfo(member.getId(), member.getName(), member.getMemberRole());

        // when & then
        assertThatThrownBy(() -> authService.getMemberAuthInfo(authInfo))
                .isInstanceOf(SecurityException.class)
                .hasMessage("회원 정보가 올바르지 않습니다. 회원가입 후 로그인해주세요.");
    }
}
