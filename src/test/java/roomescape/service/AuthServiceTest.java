package roomescape.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.model.member.Role;
import roomescape.service.dto.AuthDto;
import roomescape.model.member.MemberWithoutPassword;
import roomescape.util.TokenManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Sql("/init.sql")
@SpringBootTest
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @DisplayName("사용자 정보가 유효할 경우 JWT 토큰을 생성하여 반환한다.")
    @Test
    void should_return_token_when_valid_member() {
        String email = "treeboss@gmail.com";
        String password = "treeboss123!";
        String expected = TokenManager.create(new MemberWithoutPassword(1L, "에버", email, Role.USER));

        String actual = authService.tryLogin(new AuthDto(email, password));

        assertAll(
                () -> assertThat(actual).isNotBlank(),
                () -> assertThat(actual).isEqualTo(expected));
    }

    @DisplayName("토큰을 통해 사용자 정보를 조회한다.")
    @Test
    void should_check_login_state() {
        MemberWithoutPassword member = new MemberWithoutPassword(1L, "에버", "treeboss@gmail.com", Role.USER);
        String token = TokenManager.create(member);

        MemberWithoutPassword loginMember = authService.extractLoginMember(token);

        assertAll(
                () -> assertThat(token).isNotBlank(),
                () -> assertThat(loginMember.getId()).isEqualTo(1L));
    }
}