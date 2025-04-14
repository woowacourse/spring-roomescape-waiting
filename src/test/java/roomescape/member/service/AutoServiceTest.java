package roomescape.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.common.BaseTest;
import roomescape.member.controller.request.TokenLoginCreateRequest;
import roomescape.member.controller.response.MemberResponse;
import roomescape.member.controller.response.TokenLoginResponse;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Name;
import roomescape.member.domain.Password;
import roomescape.member.infrastructure.JwtTokenProvider;
import roomescape.member.repository.MemberJdbcRepository;

class AutoServiceTest extends BaseTest {

    @Autowired
    AutoService autoService;
    @Autowired
    MemberJdbcRepository memberJdbcRepository;
    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Test
    @DisplayName("토큰 활용하여 로그인한다.")
    void tokenLoginTest() {
        //given
        Member matt = memberJdbcRepository.save(new Name("매트"), new Email("matt.kakao"), new Password("1234"));
        String email = matt.getEmail();
        String password = matt.getPassword();

        //when
        TokenLoginResponse tokenLoginResponse = autoService.tokenLogin(new TokenLoginCreateRequest(email, password));

        //then
        assertThat(tokenLoginResponse.tokenResponse()).isNotBlank();
    }

    @Test
    @DisplayName("등록되지 않은 회원은 로그인할 수 없다.")
    void tokenLoginFailTest() {
        //given
        memberJdbcRepository.save(new Name("매트"), new Email("matt.kakao"), new Password("1234"));

        //when - then
        assertThatThrownBy(() ->
                autoService.tokenLogin(new TokenLoginCreateRequest("matt.kakao", "123")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("[ERROR] 등록되지 않은 회원입니다.");

        assertThatThrownBy(() ->
                autoService.tokenLogin(new TokenLoginCreateRequest("matt.kaka", "1234")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("[ERROR] 등록되지 않은 회원입니다."
                );

        assertThatThrownBy(() ->
                autoService.tokenLogin(new TokenLoginCreateRequest("matt.kaka", "123")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("[ERROR] 등록되지 않은 회원입니다."
                );
    }

    @Test
    @DisplayName("토큰을 활용하여 회원을 찾는다.")
    void findMemberByTokenTest() {
        //given
        Member matt = memberJdbcRepository.save(new Name("매트"), new Email("matt.kakao"), new Password("1234"));
        String token = jwtTokenProvider.createToken(matt.getEmail());

        //when
        MemberResponse userByToken = autoService.findUserByToken(token);

        //then
        assertThat(userByToken.name()).isEqualTo("매트");
    }

}
