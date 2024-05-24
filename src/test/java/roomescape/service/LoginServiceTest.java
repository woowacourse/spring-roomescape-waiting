package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberEmail;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.member.MemberRole;
import roomescape.exception.login.UnauthorizedEmailException;
import roomescape.exception.login.UnauthorizedPasswordException;
import roomescape.exception.member.DuplicatedMemberEmailException;
import roomescape.service.login.JwtTokenProvider;
import roomescape.service.login.LoginService;
import roomescape.service.login.dto.LoginCheckResponse;
import roomescape.service.login.dto.LoginRequest;
import roomescape.service.login.dto.SignupRequest;
import roomescape.service.login.dto.SignupResponse;

class LoginServiceTest extends ServiceTest {
    @Autowired
    private LoginService loginService;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private MemberRepository memberRepository;

    @Nested
    @DisplayName("로그인")
    class Login {
        private String email;
        private String password;

        @BeforeEach
        void setUp() {
            email = "user@gmail.com";
            password = "1234567890";
        }

        @Test
        void 이메일과_비밀번호로_로그인할_수_있다() {
            LoginRequest request = new LoginRequest(email, password);

            assertThatCode(() -> loginService.login(request))
                    .doesNotThrowAnyException();
        }

        @Test
        void 이메일이_틀리면_로그인할_수_없다() {
            String wrongEmail = "wrong@gmail.com";
            LoginRequest request = new LoginRequest(wrongEmail, password);

            assertThatThrownBy(() -> loginService.login(request))
                    .isInstanceOf(UnauthorizedEmailException.class);
        }

        @Test
        void 비밀번호가_틀리면_로그인할_수_없다() {
            String wrongPassword = "wrongPassword";
            LoginRequest request = new LoginRequest(email, wrongPassword);

            assertThatThrownBy(() -> loginService.login(request))
                    .isInstanceOf(UnauthorizedPasswordException.class);
        }
    }

    @Nested
    @DisplayName("인증 정보(로그인한 사용자 정보) 조회")
    class LoginCheck {
        @Test
        void 로그인한_사용자_정보를_조회할_수_있다() {
            Member member = memberRepository.findById(1L).orElseThrow();

            LoginCheckResponse response = loginService.loginCheck(member);

            assertThat(response.getName()).isEqualTo("사용자");
        }
    }

    @Nested
    @DisplayName("토큰으로 사용자 정보 조회")
    class FindByToken {
        private MemberEmail email;
        private MemberRole role;
        private String token;

        @BeforeEach
        void setUp() {
            email = new MemberEmail("user@gmail.com");
            role = MemberRole.USER;
            token = jwtTokenProvider.createToken(email, role);
        }

        @Test
        void 토큰으로_사용자_정보를_조회할_수_있다() {
            Member member = memberRepository.findById(1L).orElseThrow();

            assertThat(loginService.findMemberByToken(token))
                    .isEqualTo(member);
        }

        @Test
        void 토큰으로_사용자_역할을_조회할_수_있다() {
            assertThat(loginService.findMemberRoleByToken(token))
                    .isEqualTo(role);
        }
    }

    @Nested
    @DisplayName("회원가입")
    class Signup {
        private String email;
        private String password;
        private String name;

        @BeforeEach
        void setUp() {
            email = "user2@gmail.com";
            password = "1234567890";
            name = "사용자2";
        }

        @Test
        void 일반유저_권한으로_회원가입을_할_수_있다() {
            SignupRequest request = new SignupRequest(email, password, name);

            SignupResponse response = loginService.signup(request);

            assertThat(response.getRole())
                    .isEqualTo(MemberRole.USER);
        }

        @Test
        void 이미_존재하는_이메일로_회원가입시_예외가_발생한다() {
            String duplicatedEmail = "user@gmail.com";
            SignupRequest request = new SignupRequest(duplicatedEmail, password, name);

            assertThatThrownBy(() -> loginService.signup(request))
                    .isInstanceOf(DuplicatedMemberEmailException.class);
        }
    }
}
