package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import roomescape.domain.Password;
import roomescape.domain.Role;
import roomescape.domain.User;
import roomescape.dto.auth.LoginRequest;
import roomescape.exception.InvalidLoginException;
import roomescape.infrastructure.JwtTokenProvider;
import roomescape.repository.fake.FakeUserRepository;

class AuthServiceTest {

    private static final String SECRET = "/BWxvVt/eMsTVSq+RI9kRCrZKK38KNGIWi7ilxCg9So=";

    private FakeUserRepository userRepository;
    private JwtTokenProvider jwtProvider;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = new FakeUserRepository();
        jwtProvider = new JwtTokenProvider(SECRET, 3600000L);
        authService = new AuthService(userRepository, jwtProvider);
        userRepository.save(new User("brown@test.com", Password.ofEncrypted("pw"), "브라운", Role.MEMBER));
    }

    @Test
    void 로그인에_성공하면_토큰을_발급한다() {
        String token = authService.login(new LoginRequest("brown@test.com", "pw"));

        assertThat(jwtProvider.getUsername(token)).isEqualTo("brown@test.com");
    }

    @Test
    void 비밀번호가_틀리면_InvalidLoginException() {
        assertThatThrownBy(() -> authService.login(new LoginRequest("brown@test.com", "wrong")))
                .isInstanceOf(InvalidLoginException.class);
    }

    @Test
    void 존재하지_않는_사용자면_InvalidLoginException() {
        assertThatThrownBy(() -> authService.login(new LoginRequest("none@test.com", "pw")))
                .isInstanceOf(InvalidLoginException.class);
    }
}