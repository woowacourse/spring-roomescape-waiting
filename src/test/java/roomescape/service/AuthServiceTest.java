package roomescape.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import roomescape.domain.Member;
import roomescape.domain.Role;
import roomescape.dto.business.AccessTokenContent;
import roomescape.dto.request.LoginRequest;
import roomescape.dto.response.AccessTokenResponse;
import roomescape.exception.global.AuthorizationException;
import roomescape.exception.local.NotFoundUserException;
import roomescape.repository.UserRepository;
import roomescape.utility.JwtTokenProvider;

@DataJpaTest
public class AuthServiceTest {

    @Autowired
    private TestEntityManager entityManager;
    @Autowired
    private UserRepository userRepository;

    private JwtTokenProvider jwtTokenProvider;
    private AuthService authService;

    @BeforeEach
    void beforeEach() {
        jwtTokenProvider = new JwtTokenProvider(
                "test_secret_key_test_secret_key_test_secret_key_test_secret_key",
                60000);
        authService = new AuthService(jwtTokenProvider, userRepository);
    }

    @Nested
    @DisplayName("로그인할 수 있다.")
    public class login {

        @DisplayName("올바른 이메일과 비밀번호로 로그인이 가능하다.")
        @Test
        void canLogin() {
            // given
            Member member = entityManager.persist(
                    Member.createWithoutId(Role.GENERAL, "회원", "member@test.com", "password123!"));

            LoginRequest loginRequest = new LoginRequest("member@test.com", "password123!");

            entityManager.flush();

            // when
            AccessTokenResponse response = authService.login(loginRequest);

            // then
            AccessTokenContent actualTokenContent = jwtTokenProvider.parseAccessToken(response.accessToken());
            AccessTokenContent expectedTokenContent =
                    new AccessTokenContent(member.getId(), member.getRole(), member.getName());
            assertThat(actualTokenContent).isEqualTo(expectedTokenContent);
        }

        @DisplayName("계정이 존재하지 않을 경우 로그인이 불가능하다.")
        @Test
        void cannotLoginWithInvalidAccount() {
            // given
            Member member = entityManager.persist(
                    Member.createWithoutId(Role.GENERAL, "회원", "member@test.com", "password123!"));

            LoginRequest wrongLoginRequest = new LoginRequest("wrong@test.com", "password123!");

            entityManager.flush();

            // when & then
            assertThatThrownBy(() -> authService.login(wrongLoginRequest))
                    .isInstanceOf(NotFoundUserException.class)
                    .hasMessage("해당 유저를 찾을 수 없습니다.");
        }

        @DisplayName("비밀번호가 맞지 않을 경우 로그인이 불가능하다.")
        @Test
        void cannotLoginWithInvalidPassword() {
            // given
            Member member = entityManager.persist(
                    Member.createWithoutId(Role.GENERAL, "회원", "member@test.com", "password123!"));

            LoginRequest wrongLoginRequest = new LoginRequest("member@test.com", "wrongPassword123!");

            entityManager.flush();

            // when & then
            assertThatThrownBy(() -> authService.login(wrongLoginRequest))
                    .isInstanceOf(AuthorizationException.class)
                    .hasMessage("로그인 정보가 올바르지 않습니다.");
        }
    }
}
