package roomescape.service;

import roomescape.exception.ErrorType;
import roomescape.exception.RoomescapeException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.Password;
import roomescape.domain.Role;
import roomescape.dto.auth.command.LoginCommand;
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
    @DisplayName("로그인에 성공하면 토큰을 발급한다")
    void issuesTokenWhenLoginSucceeds() {
        String token = authService.login(new LoginCommand("brown@test.com", "pw"));

        assertThat(jwtProvider.getUsername(token)).isEqualTo("brown@test.com");
    }

    @Test
    @DisplayName("비밀번호가 틀리면 InvalidLoginException")
    void throwsInvalidLoginExceptionWhenPasswordIsWrong() {
        assertThatThrownBy(() -> authService.login(new LoginCommand("brown@test.com", "wrong")))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.INVALID_LOGIN);
    }

    @Test
    @DisplayName("존재하지 않는 사용자면 InvalidLoginException")
    void throwsInvalidLoginExceptionWhenUserDoesNotExist() {
        assertThatThrownBy(() -> authService.login(new LoginCommand("none@test.com", "pw")))
                .isInstanceOf(RoomescapeException.class)
                .extracting(ex -> ((RoomescapeException) ex).getErrorType())
                .isEqualTo(ErrorType.INVALID_LOGIN);
    }
}
