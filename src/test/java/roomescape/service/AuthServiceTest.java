package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.Password;
import roomescape.domain.Role;
import roomescape.dto.auth.command.LoginCommand;
import roomescape.exception.InvalidLoginException;
import roomescape.infrastructure.JwtTokenProvider;

class AuthServiceTest extends ServiceIntegrationTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtTokenProvider jwtProvider;

    @BeforeEach
    void setUp() {
        String hashedPassword = Password.ofEncrypted("pw").getValue();
        jdbcTemplate.update(
                "INSERT INTO users(name, username, password, role) VALUES (?, ?, ?, ?)",
                "브라운", "brown@test.com", hashedPassword, Role.MEMBER.name());
    }

    @Test
    void 로그인에_성공하면_토큰을_발급한다() {
        String token = authService.login(new LoginCommand("brown@test.com", "pw"));

        assertThat(jwtProvider.getUsername(token)).isEqualTo("brown@test.com");
    }

    @Test
    void 비밀번호가_틀리면_InvalidLoginException() {
        assertThatThrownBy(() -> authService.login(new LoginCommand("brown@test.com", "wrong")))
                .isInstanceOf(InvalidLoginException.class);
    }

    @Test
    void 존재하지_않는_사용자면_InvalidLoginException() {
        assertThatThrownBy(() -> authService.login(new LoginCommand("none@test.com", "pw")))
                .isInstanceOf(InvalidLoginException.class);
    }
}
