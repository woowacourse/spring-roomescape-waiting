package roomescape.member.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import roomescape.common.exception.AuthenticationException;
import roomescape.member.auth.jwt.JwtTokenExtractor;
import roomescape.member.auth.jwt.JwtTokenProvider;
import roomescape.member.controller.dto.LoginRequest;
import roomescape.member.controller.dto.MemberInfoResponse;
import roomescape.member.controller.dto.SignupRequest;
import roomescape.member.repository.FakeMemberRepository;
import roomescape.member.service.usecase.MemberCommandUseCase;
import roomescape.member.service.usecase.MemberQueryUseCase;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class AuthServiceTest {

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private JwtTokenExtractor jwtTokenExtractor;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        FakeMemberRepository fakeMemberRepository = new FakeMemberRepository();
        MemberService memberService = new MemberService(
                new MemberCommandUseCase(fakeMemberRepository),
                new MemberQueryUseCase(fakeMemberRepository)
        );

        authService = new AuthService(
                memberService,
                jwtTokenProvider,
                jwtTokenExtractor,
                passwordEncoder
        );
    }

    @Test
    void 이메일_비밀번호_이름으로_회원가입_한다() {
        // given
        final SignupRequest request = new SignupRequest("siso@gmail.com", "1234", "siso");

        // when & then
        assertThat(authService.signup(request).id())
                .isNotNull();
    }

    @Test
    void 이메일과_비밀번호로_로그인한다() {
        // given
        final String password = "1234";
        final MemberInfoResponse memberInfoResponse = authService.signup(new SignupRequest("siso@gmail.com", password, "siso"));

        final LoginRequest request = new LoginRequest(memberInfoResponse.email(), password);

        assertThat(authService.login(request))
                .isNotBlank();
    }

    @Test
    void 로그인_체크를_통해_로그인_상태를_확인할_수_있다() {
        // given
        final String password = "1234";
        final MemberInfoResponse memberInfo = authService.signup(new SignupRequest("siso@gmail.com", password, "siso"));
        final LoginRequest request = new LoginRequest(memberInfo.email(), password);

        final String token = authService.login(request);

        // when & then
        assertThat(authService.checkLogin(token).name())
                .isEqualTo(memberInfo.name());
    }

    @Test
    void 로그인_상태가_아니라면_로그인_체크_시_예외가_발생한다() {
        // given
        final String token = "OMG";

        // when & then
        assertThatThrownBy(() -> authService.checkLogin(token))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    void 로그인_된_사용자의_정보를_반환한다() {
        // given
        final String password = "1234";
        final MemberInfoResponse memberInfo = authService.signup(new SignupRequest("siso@gmail.com", password, "siso"));
        final LoginRequest request = new LoginRequest(memberInfo.email(), password);

        final String token = authService.login(request);

        assertThat(authService.getMemberInfo(token).id())
                .isEqualTo(memberInfo.id());
    }

    @Test
    void 쿠키를_통해_회원_정보를_받아온다() {
        // given
        final String password = "1234";
        final MemberInfoResponse memberInfo = authService.signup(new SignupRequest("siso@gmail.com", password, "siso"));
        final LoginRequest request = new LoginRequest(memberInfo.email(), password);

        final String token = authService.login(request);

        assertThat(authService.get(token).getId().getValue())
                .isEqualTo(memberInfo.id());
    }
}
