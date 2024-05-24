package roomescape.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.service.dto.AuthDto;
import roomescape.model.member.MemberWithoutPassword;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@Sql("/init.sql")
@SpringBootTest
class AuthServiceTest {

    private static final AuthDto userDto = new AuthDto("treeboss@gmail.com", "treeboss123!");

    @Autowired
    private AuthService authService;

    @DisplayName("사용자 정보를 통해 JWT 토큰을 생성한다.")
    @Test
    void should_create_token() {
        AuthDto authDto = new AuthDto(userDto.getEmail().getEmail(), userDto.getPassword().getPassword());

        String accessToken = authService.tryLogin(authDto);

        MemberWithoutPassword loginMember = authService.extractLoginMember(accessToken);
        assertAll(
                () -> assertThat(accessToken).isNotBlank(),
                () -> assertThat(loginMember.getId()).isEqualTo(1L));
    }

    @DisplayName("토큰을 통해 사용자 정보를 조회한다.")
    @Test
    void should_check_login_state() {
        String token = authService.tryLogin(userDto);

        MemberWithoutPassword loginMember = authService.extractLoginMember(token);

        assertThat(loginMember.getId()).isEqualTo(1L);
    }
}