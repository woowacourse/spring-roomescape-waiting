package roomescape.service;


import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import roomescape.common.exception.NotFoundException;
import roomescape.common.exception.UnauthorizedException;
import roomescape.dto.request.LoginRequestDto;
import roomescape.fake.FakeMemberDao;
import roomescape.model.Member;
import roomescape.model.Role;

public class AuthServiceTest {
    private final FakeMemberDao fakeUserDao = new FakeMemberDao();
    private final AuthService authService = new AuthService(null, fakeUserDao);

    private String email;
    private String password;

    @BeforeEach
    void setUp() {
        this.email = "john@example.com";
        this.password = "password";
        fakeUserDao.add(new Member("John", this.email, this.password, Role.fromValue("ADMIN")));
    }

    @Test
    @DisplayName("존재하지 않는 이메일에 대하여 로그인 요청을 하는 경우 예외가 발생한다.")
    void test1() {
        // given
        LoginRequestDto loginRequestDto = new LoginRequestDto("invalidEmail@gmail.com", "password");

        // when & then
        assertThatThrownBy(() -> authService.login(loginRequestDto))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("틀린 비밀번호로 로그인 요청을 하는 경우 예외가 발생한다.")
    void test2() {
        // given
        LoginRequestDto loginRequestDto = new LoginRequestDto(this.email, "invalidPassword");

        // when & then
        assertThatThrownBy(() -> authService.login(loginRequestDto))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    @DisplayName("존재하는 이메일과 일치하는 비밀번호로 로그인 요청을 하는 경우 예외가 발생하지 않는다.")
    void test3() {
        // given
        LoginRequestDto loginRequestDto = new LoginRequestDto(this.email, this.password);

        // when & then
        Assertions.assertDoesNotThrow(() -> authService.login(loginRequestDto));
    }
}
