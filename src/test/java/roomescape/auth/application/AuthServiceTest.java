package roomescape.auth.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import roomescape.auth.application.dto.LoginCheckResponse;
import roomescape.auth.application.dto.LoginRequest;
import roomescape.auth.exception.PasswordNotMatchedException;
import roomescape.fixture.MemberFixture;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.member.exception.MemberNotFoundException;
import roomescape.member.infrastructure.BcryptPasswordEncoder;
import roomescape.member.infrastructure.MemberRepositoryAdapter;

@ActiveProfiles("test")
@DataJpaTest
@Import({
        AuthService.class,
        TokenProvider.class,
        BcryptPasswordEncoder.class,
        MemberRepositoryAdapter.class
})
class AuthServiceTest {

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AuthService authService;

    private Member member;
    private LoginRequest loginRequest;
    private final String email = "test@example.com";
    private final String password = "password123";
    private final String name = "testUser";

    @BeforeEach
    void setUp() {
        member = MemberFixture.createMember(name, email, password);
        loginRequest = new LoginRequest(email, password);
    }

    @Test
    @DisplayName("로그인 - 성공")
    void login_success() {
        // given
        memberRepository.save(member);

        // when
        String token = authService.login(loginRequest);

        // then
        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
    }

    @Test
    @DisplayName("로그인 - 존재하지 않는 이메일로 로그인 시 예외가 발생한다")
    void login_withNonExistingEmail() {
        // given
        LoginRequest nonExistingRequest = new LoginRequest("nonexisting@example.com", password);

        // when & then
        assertThatThrownBy(() -> authService.login(nonExistingRequest))
                .isInstanceOf(MemberNotFoundException.class);
    }

    @Test
    @DisplayName("로그인 - 비밀번호가 일치하지 않으면 예외가 발생한다")
    void login_withIncorrectPassword() {
        // given
        memberRepository.save(member);
        LoginRequest wrongPasswordRequest = new LoginRequest(email, "wrongpassword");

        // when & then
        assertThatThrownBy(() -> authService.login(wrongPasswordRequest))
                .isInstanceOf(PasswordNotMatchedException.class);
    }

    @Test
    @DisplayName("로그인 체크 - 성공")
    void loginCheck_success() {
        // given
        Member savedMember = memberRepository.save(member);

        // when
        LoginCheckResponse response = authService.loginCheck(savedMember.getId());

        // then
        assertThat(response.name()).isEqualTo(name);
    }

    @Test
    @DisplayName("로그인 체크 - 존재하지 않는 회원 ID로 로그인 체크 시 예외가 발생한다")
    void loginCheck_withNonExistingId() {
        // given
        Long nonExistingId = 999L;

        // when & then
        assertThatThrownBy(() -> authService.loginCheck(nonExistingId))
                .isInstanceOf(MemberNotFoundException.class);
    }
}
