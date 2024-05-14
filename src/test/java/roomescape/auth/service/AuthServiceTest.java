package roomescape.auth.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import roomescape.auth.JwtTokenProvider;
import roomescape.auth.dto.LoginCheckResponse;
import roomescape.auth.dto.LoginRequest;
import roomescape.auth.dto.LoginResponse;
import roomescape.auth.dto.SignupRequest;
import roomescape.exception.BadRequestException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.member.repository.MemberDao;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@Import(value = {AuthService.class, JwtTokenProvider.class, MemberDao.class})
@Sql(value = {"/recreate_table.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@DisplayName("인증 서비스")
class AuthServiceTest {

    private final AuthService authService;

    @Autowired
    public AuthServiceTest(AuthService authService) {
        this.authService = authService;
    }

    @DisplayName("인증 서비스는 로그인 요청이 들어오면 액세스 토큰을 반환한다.")
    @Test
    void login() {
        // given
        LoginRequest loginRequest = new LoginRequest("test@gmail.com", "password");

        // when
        LoginResponse actual = authService.login(loginRequest);

        // then
        assertThat(actual.accessToken()).isNotNull();
    }

    @DisplayName("인증 서비스는 잘못된 이메일로 로그인 요청이 들어온 경우 예외가 발생한다.")
    @Test
    void loginWithWrongEmail() {
        // given
        LoginRequest loginRequest = new LoginRequest("wrongEmail@gmail.com", "password");

        // when & then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("존재하지 않는 멤버입니다.");
    }

    @DisplayName("인증 서비스는 잘못된 비밀번호로 로그인 요청이 들어온 경우 예외가 발생한다.")
    @Test
    void loginWithWrongPassword() {
        // given
        LoginRequest loginRequest = new LoginRequest("test@gmail.com", "wrong");

        // when & then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("비밀번호가 잘못됐습니다.");
    }

    @DisplayName("인증 서비스는 회원 가입 요청이 들어오면 가입된 사용자의 이름을 반환한다.")
    @Test
    void signup() {
        // given
        SignupRequest request = new SignupRequest("signup@gmail.com", "password", "테스터");

        // when
        LoginCheckResponse actual = authService.signup(request);

        // then
        assertThat(actual.name()).isEqualTo("테스터");
    }

    @DisplayName("인증 서비스는 중복된 이메일로 회원가입 요청이 들어올 경우 예외가 발생한다.")
    @Test
    void signupWithDuplicatedEmail() {
        // given
        SignupRequest request = new SignupRequest("duplicated@gmail.com", "password", "테스터");
        authService.signup(request);

        // when & then
        assertThatThrownBy(() -> authService.signup(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("이미 존재하는 이메일입니다.");
    }
}
