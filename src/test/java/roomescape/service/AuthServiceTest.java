package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.jdbc.Sql;
import roomescape.dto.login.LoginMember;
import roomescape.dto.login.LoginRequest;
import roomescape.dto.token.TokenDto;
import roomescape.repository.MemberRepository;

@Sql("/member-test-data.sql")
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class AuthServiceTest {

    @Autowired
    AuthService authService;

    @Autowired
    MemberRepository memberRepository;

    @Test
    void 등록된_계정으로_로그인() throws Exception {
        //given
        LoginRequest loginRequest = new LoginRequest("test@email.com", "123456");

        //when
        TokenDto token = authService.login(loginRequest);

        //then
        LoginMember loginMember = authService.extractLoginMemberByToken(token);
        assertThat(loginMember.email()).isEqualTo(loginRequest.email());
    }

    @Test
    void 등록되지_않은_이메일로_로그인할_경우_예외_발생() {
        //given
        LoginRequest loginRequest = new LoginRequest("test99@email.com", "123456");

        //when, then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 잘못된_비밀번호로_로그인할_경우_예외_발생() {
        //given
        LoginRequest loginRequest = new LoginRequest("test@email.com", "000000");

        //when, then
        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 검증되지_않은_토큰으로_로그인_계정을_확인할_경우_예외_발생() {
        //given
        TokenDto tokenDto = new TokenDto(null);

        //when, then
        assertThatThrownBy(() -> authService.extractLoginMemberByToken(tokenDto))
                .isInstanceOf(Exception.class);
    }
}
