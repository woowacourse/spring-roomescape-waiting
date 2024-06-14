package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.controller.utils.TokenUtils;
import roomescape.service.dto.AuthDto;
import roomescape.service.dto.MemberInfo;

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

        String accessToken = authService.createToken(authDto);

        MemberInfo memberInfo = TokenUtils.parseToken(accessToken);
        assertAll(
                () -> assertThat(accessToken).isNotBlank(),
                () -> assertThat(memberInfo.getId()).isEqualTo(2L));
    }

    @DisplayName("토큰을 통해 사용자 정보를 조회한다.")
    @Test
    void should_check_login_state() {
        String token = authService.createToken(userDto);

        MemberInfo loginMember = TokenUtils.parseToken(token);

        assertThat(loginMember.getId()).isEqualTo(2L);
    }
}
